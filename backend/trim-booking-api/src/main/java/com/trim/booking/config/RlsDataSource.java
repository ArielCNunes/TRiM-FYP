package com.trim.booking.config;

import com.trim.booking.tenant.TenantContext;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A DataSource wrapper that sets the PostgreSQL session variable
 * 'app.current_business_id' on every connection checkout.
 *
 * This ensures Row-Level Security policies filter data to the
 * correct tenant for every query executed during the request.
 *
 * Uses SET LOCAL so the variable is scoped to the current transaction
 * and automatically reset when the transaction completes.
 */
public class RlsDataSource extends DelegatingDataSource {

    public RlsDataSource(DataSource delegate) {
        super(delegate);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        setTenantContext(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        setTenantContext(connection);
        return connection;
    }

    private void setTenantContext(Connection connection) throws SQLException {
        if (TenantContext.isSet()) {
            Long businessId = TenantContext.getCurrentBusinessId();
            if (businessId != null) {
                try (PreparedStatement pstmt = connection.prepareStatement(
                        "SELECT set_config('app.current_business_id', ?, true)")) {
                    pstmt.setString(1, String.valueOf(businessId));
                    pstmt.execute();
                }
            }
        }
    }
}

