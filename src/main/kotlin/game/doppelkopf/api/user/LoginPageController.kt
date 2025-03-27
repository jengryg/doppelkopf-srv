package game.doppelkopf.api.user

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/v1/auth/login")
class LoginPageController {
    @GetMapping("")
    fun loginPage(): String {
        return "login-page"
    }
}