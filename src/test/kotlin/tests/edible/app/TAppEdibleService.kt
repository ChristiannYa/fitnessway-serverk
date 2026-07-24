package tests.edible.app

import com.example.domain.AppFood
import com.example.dto.AppEdibleWriteRequest
import mock.edible.buildEdibleRequestData
import tests.TAppTest
import java.util.*

abstract class TAppEdibleService : TAppTest() {

    protected suspend fun submitAppEdible(
        userId: UUID,
        name: String = "edible",
        barcode: String = "011110150974"
    ): AppFood = buildEdibleRequestData("$name #${(1000..9999).random()}").let {
        appEdibleService.submit(
            req = AppEdibleWriteRequest(
                edibleRequest = it,
                barcode = barcode
            ),
            userId = userId
        )
    }
}
