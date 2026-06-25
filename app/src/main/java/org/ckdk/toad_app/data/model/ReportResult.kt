package org.ckdk.toad_app.data.model

import org.ckdk.toad_app.data.network.model.ReportResponse

sealed class ReportResult {
    data class Success(val report: ReportResponse) : ReportResult()
    object Unauthorized : ReportResult()
    data class Error(val message: String) : ReportResult()
}

sealed class ReportListResult {
    data class Success(val reports: List<ReportResponse>) : ReportListResult()
    object Unauthorized : ReportListResult()
    data class Error(val message: String) : ReportListResult()
}
