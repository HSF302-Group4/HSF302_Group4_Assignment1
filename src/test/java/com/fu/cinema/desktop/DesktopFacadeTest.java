package com.fu.cinema.desktop;

import com.fu.cinema.desktop.facade.AuthFacade;
import com.fu.cinema.desktop.facade.BookingFacade;
import com.fu.cinema.desktop.facade.GenreFacade;
import com.fu.cinema.desktop.facade.MovieFacade;
import com.fu.cinema.desktop.model.BookingRow;
import com.fu.cinema.desktop.model.MovieRow;
import com.fu.cinema.desktop.model.PageResult;
import com.fu.cinema.desktop.session.DesktopSession;
import com.fu.cinema.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DesktopFacadeTest {

    @Autowired
    private AuthFacade authFacade;
    @Autowired private MovieFacade movieFacade;
    @Autowired private BookingFacade bookingFacade;
    @Autowired private GenreFacade genreFacade;
    @Autowired private DesktopSession session;

    @Test
    void customerAccount_cannotLoginDesktop() {                                   // BR-D1
        assertThrows(BusinessException.class, () -> authFacade.loginAdmin("an.nv@gmail.com", "Customer@123"));
    }

    @Test
    void adminAccount_canLogin() {
        assertTrue(authFacade.loginAdmin("admin@fucinema.vn", "Admin@123").isAdmin());
    }

    @Test
    void movieRows_areMappedInsideTransaction() {                                // không LazyInitializationException
        PageResult<MovieRow> page = movieFacade.search(null, null, null, "title", 0);
        assertFalse(page.items().isEmpty());
        assertTrue(page.items().stream().allMatch(m -> m.genreName() != null));
    }

    @Test
    void bookingRows_haveCustomerAndMovie() {
        PageResult<BookingRow> page = bookingFacade.search(null, null, null, null, 0);
        assertTrue(page.items().stream().allMatch(b -> b.customerName() != null && b.movieTitle() != null));
    }

    @Test
    void writeOperation_requiresAdminSession() {
        session.logout();
        assertThrows(BusinessException.class, () -> genreFacade.delete(1L));
    }
}