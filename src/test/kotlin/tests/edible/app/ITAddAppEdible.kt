package tests.edible.app

import com.example.config.RewardConfig
import com.example.config.RewardConfig.applyMultiplier
import com.example.domain.AppEdibleReport
import com.example.domain.AppFood
import com.example.domain.NutrientIdWithAmount
import com.example.domain.UserType
import com.example.dto.AppEdibleReportRequest
import com.example.dto.AppEdibleWriteRequest
import com.example.dto.EdibleWriteRequest
import com.example.exception.InvalidEdibleBarcodeException
import com.example.mappers.toList
import com.example.utils.toEnum
import kotlinx.coroutines.test.runTest
import org.junit.Test
import utils.equalsMessage
import utils.notNullMessage
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ITAddAppEdible : TAppEdibleService() {

    private fun AppFood.toUpdateVersion(
        barcode: String,
        name: String = "${this.information.base.name} (updated) #${(1..999).random()}"
    ): Pair<Int, AppEdibleWriteRequest> =
        Pair(
            this.id,
            AppEdibleWriteRequest(
                edibleRequest = EdibleWriteRequest(
                    base = this.information.base.copy(name = name),
                    nutrients = this.information.nutrients
                        .toList()
                        .map { n -> NutrientIdWithAmount(n.data.base.id, n.amount) },
                    edibleType = this.information.type.toString()
                ),
                barcode = barcode
            )
        )

    @Test
    fun `submits app edible along with its barcode`() = runTest {
        // Arrange
        val admin = createUser(userType = UserType.ADMIN)
        val barcode = "011110150974"

        // Act - submit app edible
        val appEdible = submitAppEdible(userId = admin.id, barcode = barcode)

        // Assert - app edible is present
        assertNotNull(appEdible, notNullMessage("appEdible"))

        // Assert - app edible has barcode
        val appEdibleByBarcode = appEdibleService.findByBarCode(barcode, admin.id)
        assertNotNull(appEdibleByBarcode, notNullMessage("appEdibleByBarcode"))
    }

    @Test
    fun `user can report food`() = runTest {
        // Arrange
        val admin = createUser(userType = UserType.ADMIN)
        val reporter = createUser()
        val barcode = "011110150974"
        val appEdible = submitAppEdible(userId = admin.id, barcode = barcode)

        // Act - report edible
        val report = appEdibleService.report(
            req = AppEdibleReportRequest(
                edibleId = appEdible.id,
                reasons = listOf(AppEdibleReport.Reason.INCORRECT_INFO.toString().lowercase()),
                notes = null,
                updatedEdible = appEdible.toUpdateVersion(barcode)
            ),
            userId = reporter.id
        )

        // Assert - report is found
        val (reportDaoById, _) = appFoodRepository.findReportById(report.id) ?: (null to null)
        assertNotNull(reportDaoById, notNullMessage("reportById"))

        // Assert - reported by matches
        assertEquals(reporter.id, reportDaoById.reportedBy?.value)
    }

    @Test
    fun `reviewed report is updated accordingly`() = runTest {
        // Arrange
        val admin = createUser(userType = UserType.ADMIN)
        val reporter = createUser()
        val barcode = "011110150974"
        val appEdible = submitAppEdible(userId = admin.id, barcode = barcode)
        val report = appEdibleService.report(
            req = AppEdibleReportRequest(
                edibleId = appEdible.id,
                reasons = listOf(AppEdibleReport.Reason.INCORRECT_INFO.toString().lowercase()),
                notes = null,
                updatedEdible = appEdible.toUpdateVersion(barcode)
            ),
            userId = reporter.id
        )

        // Act - Review report (as admin)
        appEdibleService.reviewReport(report.id, admin.id)

        // Assert - report is updated
        val reviewedReport = appFoodRepository.findReportById(report.id)
        assertNotNull(reviewedReport, "reviewedReport")

        val (aerDao, _) = reviewedReport
        assertEquals(AppEdibleReport.Status.REVIEWED, aerDao.status.toEnum())
        assertNotNull(aerDao.reviewedAt, notNullMessage("reviewedAt"))
        assertEquals(admin.id, aerDao.reviewedBy?.value)
    }

    @Test
    fun `users are rewared when reporting`() = runTest {
        // Arrange
        val admin = createUser(userType = UserType.ADMIN)

        val reporterAsUser = createUser()
        val reporterAsContributor = createUser(userType = UserType.CONTRIBUTOR)

        val (appEdibleA, appEdibleABarcode) = run {
            val barcode = "011110150974"
            val submission = submitAppEdible(admin.id, barcode = barcode)
            submission to barcode
        }

        val (appEdibleB, appEdibleBBarcode) = run {
            val barcode = "074312008092"
            val submission = submitAppEdible(admin.id, barcode = barcode)
            submission to barcode
        }

        val (appEdibleC, appEdibleCBarcode) = run {
            val barcode = "011110007407"
            val submission = submitAppEdible(admin.id, barcode = barcode)
            submission to barcode
        }

        val reportAFromUser = appEdibleService.report(
            req = AppEdibleReportRequest(
                edibleId = appEdibleA.id,
                reasons = listOf(AppEdibleReport.Reason.INCORRECT_INFO.toString().lowercase()),
                notes = null,
                updatedEdible = appEdibleA.toUpdateVersion(appEdibleABarcode)
            ),
            userId = reporterAsUser.id
        )
        val reportAFromContributor = appEdibleService.report(
            req = AppEdibleReportRequest(
                edibleId = appEdibleB.id,
                reasons = listOf(AppEdibleReport.Reason.INCORRECT_INFO.toString().lowercase()),
                notes = null,
                updatedEdible = appEdibleB.toUpdateVersion(appEdibleBBarcode)
            ),
            userId = reporterAsContributor.id
        )
        val reportBFromContributor = appEdibleService.report(
            req = AppEdibleReportRequest(
                edibleId = appEdibleC.id,
                reasons = listOf(AppEdibleReport.Reason.INCORRECT_INFO.toString().lowercase()),
                notes = null,
                updatedEdible = appEdibleC.toUpdateVersion(appEdibleCBarcode)
            ),
            userId = reporterAsContributor.id
        )

        val originalReporterAsUserCurrency = userWalletRepository.getBalance(reporterAsUser.id)
        val originalReporterAsContributorCurrency = userWalletRepository.getBalance(reporterAsContributor.id)

        // Act - review reports
        appEdibleService.reviewReport(reportAFromUser.id, admin.id)
        appEdibleService.reviewReport(reportAFromContributor.id, admin.id)
        appEdibleService.reviewReport(reportBFromContributor.id, admin.id)

        // Assert - original reporter currencies are 0.0
        assertEquals(0.0, originalReporterAsUserCurrency, equalsMessage("0.0", "originalReporterAsUserCurrency"))
        assertEquals(
            0.0,
            originalReporterAsContributorCurrency,
            equalsMessage("0.0", "originalReporterAsContributorCurrency")
        )

        // Assert - reporters currency increased
        assertEquals(
            RewardConfig.APP_EDIBLE_REPORT_REWARD.applyMultiplier(reporterAsUser.type),
            userWalletRepository.getBalance(reporterAsUser.id),
            equalsMessage(
                "APP_EDIBLE_REPORT_REWARD.applyMultiplier(reporterAsUser.type)",
                "updatedReporterAsUserCurrency"
            )
        )
        assertEquals(
            RewardConfig.APP_EDIBLE_REPORT_REWARD.times(2).applyMultiplier(reporterAsContributor.type),
            userWalletRepository.getBalance(reporterAsContributor.id),
            equalsMessage(
                "APP_EDIBLE_REPORT_REWARD.applyMultiplier(reporterAsContributor.type)",
                "updatedReporterAsContributorCurrency"
            )
        )
    }

    @Test
    fun `reported edible is updated upon review`() = runTest {
        // Arrange
        val admin = createUser(userType = UserType.ADMIN)
        val reporter = createUser()
        val barcode = "011110150974"
        val appEdible = submitAppEdible(userId = admin.id, barcode = barcode)
        val (updatedEdibleId, updatedEdibleReq) = appEdible.toUpdateVersion(barcode)
        val report = appEdibleService.report(
            req = AppEdibleReportRequest(
                edibleId = appEdible.id,
                reasons = listOf(AppEdibleReport.Reason.INCORRECT_INFO.toString().lowercase()),
                notes = null,
                updatedEdible = Pair(updatedEdibleId, updatedEdibleReq)
            ),
            userId = reporter.id
        )

        // Act - review report
        appEdibleService.reviewReport(report.id, admin.id)
        val reviewedReport = appFoodRepository.findReportById(report.id)
        assertNotNull(reviewedReport, "reviewedReport")

        // Assert - Edible is updated
        val appEdibleDb = appEdibleService.findById(appEdible.id, admin.id)
        assertNotNull(appEdibleDb, "appEdibleDb")
        assertEquals(updatedEdibleReq.edibleRequest.base.name, appEdibleDb.edible.information.base.name)
    }

    // ----------
    // FAIL CASES
    // ----------

    @Test
    fun `submitting an invalid barcode throws an invalid barcode exception`() = runTest {
        // Arrange
        val admin = createUser(userType = UserType.USER)
        val barcode = "123456789101112"

        // Act & Assert
        assertFailsWith<InvalidEdibleBarcodeException> {
            submitAppEdible(userId = admin.id, barcode = barcode)
        }
    }
}