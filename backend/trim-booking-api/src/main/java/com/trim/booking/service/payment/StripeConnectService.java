package com.trim.booking.service.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.trim.booking.entity.Business;
import com.trim.booking.repository.BusinessRepository;
import com.trim.booking.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeConnectService {
    private static final Logger logger = LoggerFactory.getLogger(StripeConnectService.class);

    private final BusinessRepository businessRepository;

    @Value("${app.base-domain}")
    private String baseDomain;

    @Value("${app.frontend-port}")
    private String frontendPort;

    public StripeConnectService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    private Business getCurrentBusiness() {
        Long businessId = TenantContext.getCurrentBusinessId();
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));
    }

    private String getFrontendBaseUrl() {
        String slug = TenantContext.getCurrentBusinessSlug();
        return "http://" + slug + "." + baseDomain + ":" + frontendPort;
    }

    /**
     * Create a Stripe Standard connected account for the current business
     * and return the onboarding URL.
     */
    @Transactional
    public Map<String, Object> createConnectAccount() throws StripeException {
        Business business = getCurrentBusiness();

        if (business.getStripeAccountId() != null) {
            // Account already exists, just return a fresh onboarding link
            return createAccountLink();
        }

        AccountCreateParams params = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.STANDARD)
                .build();

        Account account = Account.create(params);

        business.setStripeAccountId(account.getId());
        business.setStripeOnboardingComplete(false);
        businessRepository.save(business);

        logger.info("Created Stripe connected account {} for business {}", account.getId(), business.getId());

        return createAccountLinkForAccount(business);
    }

    /**
     * Create a fresh Stripe AccountLink for the current business.
     * Used for retry/refresh when a previous link has expired.
     */
    public Map<String, Object> createAccountLink() throws StripeException {
        Business business = getCurrentBusiness();

        if (business.getStripeAccountId() == null) {
            throw new RuntimeException("Business does not have a Stripe account. Create one first.");
        }

        return createAccountLinkForAccount(business);
    }

    private Map<String, Object> createAccountLinkForAccount(Business business) throws StripeException {
        String frontendBase = getFrontendBaseUrl();

        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(business.getStripeAccountId())
                .setRefreshUrl(frontendBase + "/admin?tab=payments&stripe=refresh")
                .setReturnUrl(frontendBase + "/admin?tab=payments&stripe=return")
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink accountLink = AccountLink.create(params);

        Map<String, Object> response = new HashMap<>();
        response.put("url", accountLink.getUrl());
        return response;
    }

    /**
     * Check onboarding status for the current business's connected account.
     */
    @Transactional
    public Map<String, Object> checkOnboardingStatus() throws StripeException {
        Business business = getCurrentBusiness();

        Map<String, Object> response = new HashMap<>();

        if (business.getStripeAccountId() == null) {
            response.put("connected", false);
            response.put("chargesEnabled", false);
            response.put("detailsSubmitted", false);
            return response;
        }

        Account account = Account.retrieve(business.getStripeAccountId());

        boolean chargesEnabled = Boolean.TRUE.equals(account.getChargesEnabled());
        boolean detailsSubmitted = Boolean.TRUE.equals(account.getDetailsSubmitted());
        boolean onboardingComplete = chargesEnabled && detailsSubmitted;

        // Update local state if it changed
        if (onboardingComplete != Boolean.TRUE.equals(business.getStripeOnboardingComplete())) {
            business.setStripeOnboardingComplete(onboardingComplete);
            businessRepository.save(business);
        }

        response.put("connected", true);
        response.put("chargesEnabled", chargesEnabled);
        response.put("detailsSubmitted", detailsSubmitted);
        return response;
    }
}
