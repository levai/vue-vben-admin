import type { EventHandlerRequest, H3Event } from 'h3';

import { deleteCookie, getCookie, setCookie } from 'h3';

export function clearRefreshTokenCookie(event: H3Event<EventHandlerRequest>) {
  deleteCookie(event, 'refreshToken', {
    httpOnly: true,
    sameSite: 'none',
    secure: true,
  });
}

export function setRefreshTokenCookie(
  event: H3Event<EventHandlerRequest>,
  refreshToken: string,
) {
  setCookie(event, 'refreshToken', refreshToken, {
    httpOnly: true,
    maxAge: 7 * 24 * 60 * 60, // 7天，与后端保持一致（单位：秒）
    sameSite: 'none',
    secure: true,
  });
}

export function getRefreshTokenFromCookie(event: H3Event<EventHandlerRequest>) {
  const refreshToken = getCookie(event, 'refreshToken');
  return refreshToken;
}
