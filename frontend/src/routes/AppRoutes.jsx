import { Navigate, Outlet, Route, Routes } from "react-router-dom";

import AuthLayout from "../layouts/AuthLayout";

import PublicRoute from "./PublicRoute";
import ProtectedRoute from "./ProtectedRoute";

import Index from "../pages/public/Index";

import Login from "../pages/auth/Login";
import Register from "../pages/auth/Register";
import ForgotPassword from "../pages/auth/ForgotPassword";
import ResetPassword from "../pages/auth/ResetPassword";
import ForceChangePassword from "../pages/auth/ForceChangePassword";

import Unauthorized from "../pages/errors/Unauthorized";
import NotFound from "../pages/errors/NotFound";
import Maintenance from "../pages/errors/Maintenance";

import SuperAdminDashboard from "../pages/superadmin/SuperAdminDashboard";
import SuperAdminAddCompanyAdmin from "../pages/superadmin/SuperAdminAddCompanyAdmin";
import SuperAdminUsers from "../pages/superadmin/SuperAdminUsers";
import SuperAdminLogs from "../pages/superadmin/SuperAdminLogs";

import AdminDashboard from "../pages/admin/AdminDashboard";
import AdminProjects from "../pages/admin/AdminProjects";
import AdminSlaMonitoring from "../pages/admin/AdminSlaMonitoring";
import AdminSlaConfiguration from "../pages/admin/AdminSlaConfiguration";
import AdminUserManagement from "../pages/admin/AdminUserManagement";
import AdminReports from "../pages/admin/AdminReports";

import ManagerDashboard from "../pages/manager/ManagerDashboard";
import ManagerAssignedProjects from "../pages/manager/ManagerAssignedProjects";
import ManagerIssues from "../pages/manager/ManagerIssues";
import AssignIssues from "../pages/manager/AssignIssues";
import ManagerResolvedIssues from "../pages/manager/ManagerResolvedIssues";
import ManagerSlaMonitoring from "../pages/manager/ManagerSlaMonitoring";
import ManagerWorkload from "../pages/manager/ManagerWorkload";
import Team from "../pages/manager/Team";
import ManagerReports from "../pages/manager/ManagerReports";

import EngineerDashboard from "../pages/engineer/EngineerDashboard";
import EngineerProjectDetails from "../pages/engineer/EngineerProjectDetails";
import MyEngineerIssues from "../pages/engineer/MyIssues";
import EngineerSlaPolicies from "../pages/engineer/EngineerSlaPolicies";
import EngineerWorkload from "../pages/engineer/EngineerWorkload";
import EngineerSolvedIssues from "../pages/engineer/EngineerSolvedIssues";
import EngineerReports from "../pages/engineer/EngineerReports";

import UserDashboard from "../pages/user/UserDashboard";
import UserProjectDetails from "../pages/user/UserProjectDetails";
import UserCreateIssue from "../pages/user/UserCreateIssue";
import UserIssues from "../pages/user/UserIssues";
import UserIssueDetails from "../pages/user/UserIssueDetails";
import UserReports from "../pages/user/UserReports";

import EditProfile from "../pages/profile/EditProfile";
import ChangePassword from "../pages/profile/ChangePassword";

export default function AppRoutes() {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <PublicRoute>
            <Index />
          </PublicRoute>
        }
      />

      <Route
        path="/login"
        element={
          <PublicRoute>
            <Login />
          </PublicRoute>
        }
      />

      <Route
        path="/register"
        element={
          <PublicRoute>
            <Register />
          </PublicRoute>
        }
      />
      <Route path="/authentication/register" element={<Navigate to="/register" replace />} />

      <Route
        path="/forgot-password"
        element={
          <PublicRoute>
            <ForgotPassword />
          </PublicRoute>
        }
      />

      <Route
        path="/reset-password"
        element={
          <PublicRoute>
            <ResetPassword />
          </PublicRoute>
        }
      />

      <Route path="/unauthorized" element={<Unauthorized />} />
      <Route path="/maintenance" element={<Maintenance />} />

      <Route
        element={
          <ProtectedRoute allowedRoles={["SUPER_ADMIN", "ADMIN", "MANAGER", "ENGINEER", "USER"]}>
            <AuthLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/force-change-password" element={<ForceChangePassword />} />

        <Route
          path="/superadmin"
          element={
            <ProtectedRoute allowedRoles={["SUPER_ADMIN"]}>
              <Outlet />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<SuperAdminDashboard />} />
          <Route path="add-company-admin" element={<SuperAdminAddCompanyAdmin />} />
          <Route path="users" element={<SuperAdminUsers />} />
          <Route path="logs" element={<SuperAdminLogs />} />
        </Route>

        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <Outlet />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<AdminDashboard />} />
          <Route path="projects" element={<AdminProjects />} />
          <Route path="sla-monitoring" element={<AdminSlaMonitoring />} />
          <Route path="sla-config" element={<AdminSlaConfiguration />} />
          <Route path="users" element={<AdminUserManagement />} />
          <Route path="reports" element={<AdminReports />} />
        </Route>

        <Route
          path="/manager"
          element={
            <ProtectedRoute allowedRoles={["MANAGER"]}>
              <Outlet />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<ManagerDashboard />} />
          <Route path="projects" element={<ManagerAssignedProjects />} />
          <Route path="projects/:projectId" element={<ManagerAssignedProjects />} />
          <Route path="issues" element={<ManagerIssues />} />
          <Route path="assign-issues" element={<AssignIssues />} />
          <Route path="resolved-issues" element={<ManagerResolvedIssues />} />
          <Route path="sla-monitoring" element={<ManagerSlaMonitoring />} />
          <Route path="workload" element={<ManagerWorkload />} />
          <Route path="team-users" element={<Team />} />
          <Route path="reports" element={<ManagerReports />} />
        </Route>

        <Route
          path="/engineer"
          element={
            <ProtectedRoute allowedRoles={["ENGINEER"]}>
              <Outlet />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<EngineerDashboard />} />
          <Route path="project" element={<EngineerProjectDetails />} />
          <Route path="project/:projectId" element={<EngineerProjectDetails />} />
          <Route path="issues" element={<MyEngineerIssues />} />
          <Route path="sla-policies" element={<EngineerSlaPolicies />} />
          <Route path="workload" element={<EngineerWorkload />} />
          <Route path="solved-issues" element={<EngineerSolvedIssues />} />
          <Route path="reports" element={<EngineerReports />} />
        </Route>

        <Route
          path="/user"
          element={
            <ProtectedRoute allowedRoles={["USER"]}>
              <Outlet />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<UserDashboard />} />
          <Route path="project" element={<UserProjectDetails />} />
          <Route path="project/:projectId" element={<UserProjectDetails />} />
          <Route path="create-issue" element={<UserCreateIssue />} />
          <Route path="issues" element={<UserIssues />} />
          <Route path="issues/:issueId" element={<UserIssueDetails />} />
          <Route path="reports" element={<UserReports />} />
        </Route>

        <Route
          path="/profile/edit"
          element={
            <ProtectedRoute allowedRoles={["SUPER_ADMIN", "ADMIN", "MANAGER", "ENGINEER", "USER"]}>
              <EditProfile />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile/change-password"
          element={
            <ProtectedRoute allowedRoles={["SUPER_ADMIN", "ADMIN", "MANAGER", "ENGINEER", "USER"]}>
              <ChangePassword />
            </ProtectedRoute>
          }
        />
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
