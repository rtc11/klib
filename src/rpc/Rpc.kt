package rpc

// https://www.rfc-editor.org/rfc/rfc5531


class RpcMsg(
    val xid: UInt,
    val mtype: MsgType,
    val body: Body,
) {
    fun body(): Body = when (mtype) {
        MsgType.CALL -> body as CallBody 
        MsgType.REPLY -> body as ReplyBody
    }
}

sealed interface Body 
sealed interface Reply

data class CallBody(
    val rpcvers: UInt = 2u,
    val prog: UInt,
    val vers: UInt,
    val proc: UInt,
    val cred: OpaqueAuth,
    val verf: OpaqueAuth,
    val args: ByteArray,
): Body

data class ReplyBody(
    val stat: ReplyStat,
    val reply: Reply,
): Body {
    fun reply(): Reply = when (stat) {
        ReplyStat.MSG_ACCEPTED -> reply as Accepted
        ReplyStat.MSG_DENIED -> reply as Denied
    }
}
data class Accepted(
    val verf: OpaqueAuth,
    val stat: AcceptStat,
    val reply_data: ReplyData,
): Reply {
    fun replyData(): ReplyData = when(stat) {
        AcceptStat.SUCCESS -> reply_data as Empty
        AcceptStat.PROG_MISMATCH -> reply_data as MismatchInfo
        else -> reply_data as Empty
    }
    sealed interface ReplyData
    data object Empty: ReplyData
    data class MismatchInfo(val low: UInt, val high: UInt): ReplyData
}
sealed interface Denied: Reply
data class DeniedAuth(val stat: AuthStat): Denied
data class DeniedRpc(val stat: AuthStat): Denied

enum class AuthFlavor {
    AUTH_NONE,
    AUTH_SYS,
    AUTH_SHORT,
    AUTH_DH,
    RPCSEC_GSS
}

data class OpaqueAuth(
    val flavor: AuthFlavor,
    val body: ByteArray, // max 400 bytes
)

enum class MsgType { CALL, REPLY, }
enum class ReplyStat { MSG_ACCEPTED, MSG_DENIED, }
enum class AcceptStat {
    /* RPC executed successfully */
    SUCCESS,
    /* remote hasn't exported program */
    PROG_UNAVAIL,
    /* remote can't support version */
    PROG_MISMATCH,
    /* program can't support procedure */
    PROC_UNAVAIL,
    /* procedure can't decode params */
    GARBAGE_ARGS,
    /* errors like memory allocation failure */
    SYSTEM_ERR,
}
enum class RejectStat {
    /* RPC version number != 2 */
    RPC_MISMATCH,
    /* remote can't authenticate caller */
    AUTH_ERROR,
}
enum class AuthStat {
    /* success */
    AUTH_OK,
// ╭──────────────────────╮  
// │ failed at remote end │
// ╰──────────────────────╯ 
    /* bad credentials */
    AUTH_BADCRED,
    /* client must begin new session */
    AUTH_REJECTEDCRED,
    /* bad verifier */
    AUTH_BADVERF,
    /* verifier expired or replayed */
    AUTH_REJECTEDVERF,
    /* rejected for security reasons */
    AUTH_TOOWEAK,
// ╭────────────────╮  
// │ failed locally │
// ╰────────────────╯ 
    /* bogus response */
    AUTH_INVALIDRESP,
    /* unknown reason */
    AUTH_FAILED,

    /* kerberos generic error */
    AUTH_KERB_GENERIC,
    /* time of credential expired */
    AUTH_TIMEEXPIRE,
    /* problem with ticket file */
    AUTH_TKT_FILE,
    /* cant decode authenticator */
    AUTH_DECODE,
    /* wrong net address in ticket */
    AUTH_NET_ADDR,

    /* no credentials for user */
    RPCSEC_GSS_CREDPROBLEM,
    /* problem with context */
    RPCSEC_GSS_CTXPROBLEM,
}
