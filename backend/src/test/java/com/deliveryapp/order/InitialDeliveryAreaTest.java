package com.deliveryapp.order;

import com.deliveryapp.account.AccountService;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class InitialDeliveryAreaTest {
  @Test void doesNotInsertDefaultAreasOnStartup() {
    var repository = mock(DeliveryAreaRepository.class);
    new DeliveryAreaController(repository, mock(AccountService.class)).run();
    verifyNoInteractions(repository);
  }
}
