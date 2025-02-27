package com.sonikro.flutter_okta_sdk.okta.operations

import android.app.Activity
import com.okta.oidc.AuthorizationStatus
import com.okta.oidc.ResultCallback
import com.okta.oidc.Tokens
import com.okta.oidc.clients.sessions.SessionClient
import com.okta.oidc.util.AuthorizationException
import com.sonikro.flutter_okta_sdk.okta.entities.Constants
import com.sonikro.flutter_okta_sdk.okta.entities.Errors
import com.sonikro.flutter_okta_sdk.okta.entities.OktaClient
import com.sonikro.flutter_okta_sdk.okta.entities.PendingOperation
import java.lang.Exception

fun registerCallback(activity: Activity) {
    val sessionClient: SessionClient = OktaClient.getWebClient().sessionClient

    try{
        // Try to unregister callbacks loaded by signIn/signOut methods
        OktaClient.getWebClient().unregisterCallback()
    }catch(ex: Exception){}

    OktaClient.getWebClient().registerCallback(object : ResultCallback<AuthorizationStatus?, AuthorizationException?> {
        override fun onSuccess(status: AuthorizationStatus) {
            val result = mutableMapOf<Any, Any?>()

            if (status == AuthorizationStatus.AUTHORIZED) {
                try {
                    val tokens: Tokens = sessionClient.tokens

                    result[Constants.RESOLVE_TYPE_KEY] = Constants.AUTHORIZED
                    result[Constants.ACCESS_TOKEN_KEY] = tokens.accessToken

                    PendingOperation.success(result)
                    OktaClient.getWebClient().cancel()
                } catch (e: AuthorizationException) {
                    result[Constants.ERROR_CODE_KEY] = Errors.SIGN_IN_FAILED.errorCode
                    result[Constants.ERROR_MSG_KEY] = Errors.SIGN_IN_FAILED.errorMessage

                    PendingOperation.success(result)
                }
            } else if (status == AuthorizationStatus.SIGNED_OUT) {
                sessionClient.clear()
                result[Constants.RESOLVE_TYPE_KEY] = Constants.SIGNED_OUT
                PendingOperation.success(result)
            }
        }

        override fun onError(msg: String?, exception: AuthorizationException?) {
            val result = mutableMapOf<Any, Any?>()
            result[Constants.ERROR_CODE_KEY] = Errors.OKTA_OIDC_ERROR.errorMessage
            result[Constants.ERROR_MSG_KEY] = msg

            PendingOperation.success(result)
        }

        override fun onCancel() {
            val result = mutableMapOf<Any, Any?>()
            result[Constants.RESOLVE_TYPE_KEY] = Constants.CANCELLED
            PendingOperation.success(result)
        }
    }, activity)
}