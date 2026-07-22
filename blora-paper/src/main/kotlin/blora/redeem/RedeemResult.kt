package blora.redeem

import blora.database.mail.dao.MailDao

sealed interface RedeemResult {
    data class Success(val mail: MailDao) : RedeemResult
    data object NotFound : RedeemResult
    data object AlreadyUsedByPlayer : RedeemResult
    data object OneUseAlreadyConsumed : RedeemResult
    data object Failed : RedeemResult
}
