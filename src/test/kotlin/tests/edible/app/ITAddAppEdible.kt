package tests.edible.app

import com.example.domain.User
import com.example.domain.UserType
import com.example.exception.InvalidEdibleBarcodeException
import com.example.mapping.UDao
import com.example.utils.suspendTransaction
import kotlinx.coroutines.test.runTest
import mock.user.buildUserRegisterData
import org.junit.Test
import utils.createAndGetUserData
import utils.notNullMessage
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ITAddAppEdible : TAppEdibleService() {

    private suspend fun createAuthor(): User {
        val (author, _) = createAndGetUserData(
            this.authService,
            this.userRepository,
            buildUserRegisterData()
        )

        // Make author an admin
        suspendTransaction {
            UDao
                .findById(author.id)
                ?.let { foundAuthorDao ->
                    foundAuthorDao.userType = UserType.ADMIN
                }
        }

        return this.userRepository.findById(author.id)!!
    }

    @Test
    fun `submits app edible along with its barcode`() = runTest {
        // Arrange
        val author = createAuthor()
        val barcode = "011110150974"

        // Act - submit app edible
        val appEdible = submitAppEdible(userId = author.id, barcode = barcode)

        // Assert - app edible is present
        assertNotNull(appEdible, notNullMessage("appEdible"))

        // Assert - app edible has barcode
        val appEdibleByBarcode = appEdibleService.findByBarCode(barcode, author.id)
        assertNotNull(appEdibleByBarcode, notNullMessage("appEdibleByBarcode"))
    }

    // ----------
    // FAIL CASES
    // ----------

    @Test
    fun `submitting an invalid barcode throws an invalid barcode exception`() = runTest {
        // Arrange
        val author = createAuthor()
        val barcode = "123456789101112"

        // Act & Assert
        assertFailsWith<InvalidEdibleBarcodeException> {
            submitAppEdible(userId = author.id, barcode = barcode)
        }
    }
}