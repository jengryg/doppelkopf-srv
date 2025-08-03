package game.doppelkopf.adapter.api.user

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * This controller provides a simple html page with login form to use the form login with spring security directly.
 */
@Controller
@RequestMapping("/v1/auth/login")
class LoginPageController {
    @GetMapping("")
    fun loginPage(): String {
        return "login-page"
    }
}