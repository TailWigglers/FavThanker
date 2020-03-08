package net.tailwigglers.favthanker.tasks;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import javafx.concurrent.Task;
import net.tailwigglers.favthanker.gui.Model;
import net.tailwigglers.favthanker.gui.View;
import net.tailwigglers.favthanker.util.Group;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class GetCaptchaTask extends Task<Void> {

    private Model model;
    private View view;

    private String tempUsername;
    private ArrayList<String> tempMessages;
    private ArrayList<Group> tempGroups;
    private HtmlPage captchaLoginPage;

    GetCaptchaTask(final Model model,
                   final View view,
                   final String tempUsername,
                   final ArrayList<String> tempMessages,
                   final ArrayList<Group> tempGroups) {
        this.model = model;
        this.view = view;
        this.tempUsername = tempUsername;
        this.tempMessages = tempMessages;
        this.tempGroups = tempGroups;
    }

    @Override
    protected Void call() throws Exception
    {
        // Get the login page
        HtmlPage loginPage = model.getWebClient().getPage("https://www.furaffinity.net/login/");
        HtmlAnchor anchor = loginPage.getAnchorByHref("/login/?mode=imagecaptcha");
        captchaLoginPage = anchor.click();

        // Get and save captcha
        HtmlImage captcha = captchaLoginPage.getHtmlElementById("captcha_img");
        captcha.saveAs(new File("captcha.jpg"));
        return null;
    }

    @Override
    protected void failed() {
        view.setStateError(this.getException());
    }

    @Override
    protected void succeeded()
    {
        // Show dialog and get result
        Optional<String[]> result = view.showLoginDialog();

        // Check if captcha was actually entered
        if (result.isPresent())
        {
            // Run login task
            new Thread(new LoginTask(
                    model,
                    view,
                    tempUsername,
                    result.get()[0],
                    tempMessages,
                    tempGroups,
                    captchaLoginPage,
                    result.get()[1]
            )).start();
        }
        else {
            view.setVeilVisible(false);
        }
    }
}