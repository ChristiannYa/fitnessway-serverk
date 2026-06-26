package tests.edible.app

import com.example.config.RewardConfig
import com.example.config.RewardConfig.applyMultiplier
import com.example.domain.AppEdibleReport
import com.example.domain.UserType
import com.example.dto.AppEdibleReportRequest
import com.example.exception.InvalidEdibleBarcodeException
import kotlinx.coroutines.test.runTest
import org.junit.Test
import utils.equalsMessage
import utils.notNullMessage
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ITAddAppEdible : TAppEdibleService() {

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
        val appEdible = submitAppEdible(admin.id, "011110150974")

        // Act - report edible
        val report = appEdibleService.report(
            req = AppEdibleReportRequest(
                edibleId = appEdible.id,
                reason = AppEdibleReport.Reason.INCORRECT_INFO.toString().lowercase(),
                notes = null
            ),
            userId = reporter.id
        )

        // Assert - report is found
        val reportDaoById = appFoodRepository.findReportById(report.id)
        assertNotNull(reportDaoById, notNullMessage("reportById"))

        // Assert - reported by matches
        assertEquals(reporter.id, reportDaoById.reportedBy?.value)
    }

    @Test
    fun `admin reviewed report updates report`() = runTest {
        // Arrange
        val admin = createUser(userType = UserType.ADMIN)
        val reporter = createUser()
        val appEdible = submitAppEdible(admin.id, "011110150974")
        val report = appEdibleService.report(
            req = AppEdibleReportRequest(
                edibleId = appEdible.id,
                reason = AppEdibleReport.Reason.INCORRECT_INFO.toString().lowercase(),
                notes = null
            ),
            userId = reporter.id
        )

        // Act - Review report (as admin)
        val reviewedReport = appEdibleService.reviewReport(report.id, admin.id)

        // Assert - report is updated
        assertEquals(AppEdibleReport.Status.REVIEWED, reviewedReport.status)
        assertNotNull(reviewedReport.reviewedAt, notNullMessage("reviewedAt"))
        assertEquals(admin.id, reviewedReport.reviewedBy)
    }

    @Test
    fun `admin reviewed report rewards users appropiately`() = runTest {
        // Arrange
        val admin = createUser(userType = UserType.ADMIN)

        val reporterAsUser = createUser()
        val reporterAsContributor = createUser(userType = UserType.CONTRIBUTOR)

        val appEdibleA = submitAppEdible(admin.id, barcode = "011110150974")
        val appEdibleB = submitAppEdible(admin.id, barcode = "074312008092")
        val appEdibleC = submitAppEdible(admin.id, barcode = "011110007407")

        val reportAFromUser = appEdibleService.report(
            req = AppEdibleReportRequest(
                edibleId = appEdibleA.id,
                reason = AppEdibleReport.Reason.INCORRECT_INFO.toString().lowercase(),
                notes = null
            ),
            userId = reporterAsUser.id
        )
        val reportAFromContributor = appEdibleService.report(
            req = AppEdibleReportRequest(
                edibleId = appEdibleB.id,
                reason = AppEdibleReport.Reason.INCORRECT_INFO.toString().lowercase(),
                notes = null
            ),
            userId = reporterAsContributor.id
        )
        val reportBFromContributor = appEdibleService.report(
            req = AppEdibleReportRequest(
                edibleId = appEdibleC.id,
                reason = AppEdibleReport.Reason.INCORRECT_INFO.toString().lowercase(),
                notes = null
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