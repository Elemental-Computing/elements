/* tslint:disable */
import { Session } from './session';
export interface SessionCreation {

  /**
   * The Session Secret to pass to subsequent requests through headers.
   */
  sessionSecret?: string;

  /**
   * The Session object generated by the request.
   */
  session?: Session;
}
