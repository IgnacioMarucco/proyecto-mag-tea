import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

const isAppSubdomain = () => {
  const h = window.location.hostname;
  return h.startsWith('app.') || (!h.includes('localhost') && h !== '127.0.0.1');
};

export const appSubdomainGuard: CanActivateFn = () => {
  const router = inject(Router);
  return isAppSubdomain() || router.createUrlTree(['/']);
};

export const publicSubdomainGuard: CanActivateFn = () => {
  const router = inject(Router);
  return !isAppSubdomain() || router.createUrlTree(['/login']);
};
