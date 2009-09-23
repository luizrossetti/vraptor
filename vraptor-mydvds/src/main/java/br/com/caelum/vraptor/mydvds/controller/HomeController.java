package br.com.caelum.vraptor.mydvds.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.ioc.ComponentFactory;
import br.com.caelum.vraptor.mydvds.dao.DefaultUserDao;
import br.com.caelum.vraptor.mydvds.dao.UserDao;
import br.com.caelum.vraptor.mydvds.interceptor.UserInfo;
import br.com.caelum.vraptor.mydvds.model.User;
import br.com.caelum.vraptor.validator.Validations;
import br.com.caelum.vraptor.view.Results;

/**
 * This class will be responsible to login/logout users.
 * We will use VRaptor URI conventions for this class.
 *
 * For accessing the method doStuff of the class SomethingController,
 * the URI is: /something/doStuff
 *
 */
@Resource
public class HomeController {

    private final Result result;
    private final Validator validator;
    private final UserInfo userInfo;
	private final UserDao dao;

	/**
	 * You can receive any dependency on constructor. If VRaptor knows all dependencies, this
	 * class will be created with no problem. You can use as dependencies:
	 * - all VRaptor components, e.g {@link Result} and {@link Validator}
	 * - all of your classes annotated with @Component, e.g {@link DefaultUserDao}
	 * - all of the classes that have a {@link ComponentFactory}, e.g {@link Session} or {@link SessionFactory}
	 */
	public HomeController(UserDao dao, UserInfo userInfo, Result result, Validator validator) {
	    this.dao = dao;
		this.result = result;
	    this.validator = validator;
        this.userInfo = userInfo;
	}

	/**
	 * Since we are using the convention, the URI for this method is
	 * /home/login
	 *
	 * The method parameters are set with request parameters named
	 * login and password. Thus if we have the request:
	 *
	 * POST /home/login
	 * login=john
	 * password=nobodyknows
	 *
	 * VRaptor will call:
	 * homeController.login("john", "nobodyknows");
	 */
	public void login(String login, String password) {
		// search for the user in the database
		final User currentUser = dao.find(login, password);

		// if no user is found, adds an error message to the validator
		// "invalid_login_or_password" is the message key from messages.properties,
		// and that key is used with the fmt taglib in index.jsp, for example: <fmt:message key="error.key">
		validator.checking(new Validations() {{
		    that(currentUser, is(notNullValue()), "login", "invalid_login_or_password");
		}});
		validator.onErrorUse(Results.page()).of(HomeController.class).index();

		// the login was valid, add user to session
		userInfo.login(currentUser);

		// we don't want to go to default page (/WEB-INF/jsp/home/login.jsp)
		// we want to redirect to the user's home
		result.use(Results.logic()).redirectTo(UsersController.class).home();
	}

	/**
	 * Using the convention, the URI for this method is
	 * /home/logout
	 */
	public void logout() {
	    userInfo.logout();
	    // after logging out, we want to be redirected to home index.
	    result.use(Results.logic()).redirectTo(HomeController.class).index();
	}

	/**
	 * We should not provide direct access to jsps, so we need to have an empty method
	 * for redirecting to jsp. In this case we will use the root URI, which will be
	 * redirected to jsp /WEB-INF/jsp/login/index.jsp
	 */
	@Path("/")
	public void index() {
	}

}
