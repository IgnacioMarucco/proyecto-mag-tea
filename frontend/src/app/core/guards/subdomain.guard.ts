import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

const isAppSubdomain = () => window.location.hostname.startsWith('app.');

export const appSubdomainGuard: CanActivateFn = () => {
  const router = inject(Router);
  return isAppSubdomain() || router.createUrlTree(['/']);
};

export const publicSubdomainGuard: CanActivateFn = () => {
  const router = inject(Router);
  return !isAppSubdomain() || router.createUrlTree(['/login']);
};
