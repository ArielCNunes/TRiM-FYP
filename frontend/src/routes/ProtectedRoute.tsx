import { type ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useAppSelector } from "../store/hooks";

interface ProtectedRouteProps {
    roles?: string[];
    children: ReactNode;
}

export default function ProtectedRoute({ roles, children }: ProtectedRouteProps) {
    const user = useAppSelector((state) => state.auth.user);

    if (!user) return <Navigate to="/auth" replace />;
    if (roles && !roles.includes(user.role)) return <Navigate to="/" replace />;

    return <>{children}</>;
}
