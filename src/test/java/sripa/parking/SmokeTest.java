package sripa.parking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sripa.parking.api.ParkingController;

@SpringBootTest
class SmokeTest {

	@Autowired
	private ParkingController controller;

	@Test
	void contextLoads() {
		Assertions.assertNotNull(controller);
	}

}
