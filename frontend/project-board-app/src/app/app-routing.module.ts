import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminGuard } from './_guards/admin.guard';
import { AuthGuard } from './_guards/auth.guard';
import { ProjectResolverService } from './_services/project-resolver.service';
import { AdminUiComponent } from './admin-ui/admin-ui.component';
import { LoginComponent } from './login/login.component';
import { OverviewComponent } from './overview/overview.component';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';
import { EmployeeResolverService } from './_services/employee-resolver.service';
import { ProjectsResolverService } from './_services/projects-resolver.service';
import { ProjectRequestComponent } from './project-request/project-request.component';
import { UserUiComponent } from './user-ui/user-ui.component';

const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'admin/:id',
    component: AdminUiComponent,
    resolve: {
      employees: EmployeeResolverService
    },
    canActivate: [AuthGuard, AdminGuard]
  },
  {
    path: 'admin',
    component: AdminUiComponent,
    resolve: {
      employees: EmployeeResolverService
    },
    canActivate: [AuthGuard, AdminGuard]
  },
  {
    path: 'projects/:id/request',
    component: ProjectRequestComponent,
    resolve: {
      project: ProjectResolverService
    }
  },
  {
    path: 'projects/:key',
    component: UserUiComponent,
    resolve: {
      projects: ProjectsResolverService
    },
    canActivate: [AuthGuard]
  },
  {
    path: 'projects',
    component: UserUiComponent,
    resolve: {
      projects: ProjectsResolverService
    },
    canActivate: [AuthGuard]
  },
  {
    path: 'overview',
    component: OverviewComponent,
    canActivate: [AuthGuard]
  },
  {
    path: '',
    redirectTo: 'projects',
    pathMatch: 'full'
  },
  {
    path: '**',
    component: PageNotFoundComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: [ProjectsResolverService, EmployeeResolverService]
})
export class AppRoutingModule {
}
