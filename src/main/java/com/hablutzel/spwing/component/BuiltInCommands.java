/*
 * Copyright © 2023, Hablutzel Consulting, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hablutzel.spwing.component;

import com.hablutzel.spwing.ApplicationConfiguration;
import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.annotations.*;
import com.hablutzel.spwing.context.DocumentSession;
import com.hablutzel.spwing.model.Saveable;
import com.hablutzel.spwing.util.FileChooserUtils;
import com.hablutzel.spwing.util.WindowUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;


/**
 * BuildInCommands provides built-in support for certain commands.
 * These commands will be available by default, but since this document component
 * is the lowest level of the document component stack the commands may be intercepted
 * by handlers that are defined higher in the stack. <br>
 * The commands handled by this class are:
 * <ul>
 *     <li>cmdNOP: Do nothing</li>
 *     <li>cmdSave: Save the model to a file if possible</li>
 *     <li>cmdClose: Close the active window</li>
 *     <li>cmdQuit: Close all open window and exit</li>
 *     <li>cmdNew: Create a new instance of the primary model</li>
 *     <li>cmdOpen: Read the primary model from a file if possible</li>
 * </ul>
 * @author Bob Hablutzel
 */
@Handler
@MenuSource
@Component
@Slf4j
@RequiredArgsConstructor
public final class BuiltInCommands implements MessageSourceAware {

    private final Spwing spwing;
    private final FileChooserUtils fileChooserUtils;

    @Setter
    private MessageSource messageSource;
    /**
     * The default open behavior looks for a single class annotated with
     * {@link Model}, or, if there are more than one classes annotated with
     * {@link Model} the one that also sets {@link Model#primary()}. Having
     * found that, the class create a new instance of that model and start a
     * new document scope based on that model
     *
     * @param applicationConfiguration The {@link ApplicationConfiguration} instance
     */
    @HandlerFor("cmdOpen" )
    @SuppressWarnings("unused")
    public void defaultOpenBehavior(ApplicationConfiguration applicationConfiguration) {
        withPrimaryModel(applicationConfiguration, spwing::openModel);
    }


    /**
     * The default new behavior looks for a single class annotated with
     * {@link Model}, or, if there are more than one classes annotated with
     * {@link Model} the one that also sets {@link Model#primary()}. Having
     * found that, the class create a new instance of that model and start a
     * new document scope based on that model
     *
     * @param applicationConfiguration The {@link ApplicationConfiguration} instance
     */
    @HandlerFor("cmdNew" )
    @SuppressWarnings("unused")
    public void defaultNewBehavior(ApplicationConfiguration applicationConfiguration) {
        log.debug( "Executing default new behavior" );
        withPrimaryModel(applicationConfiguration, spwing::newModel);
    }


    /**
     * withPrimaryModel is used with the {@link #defaultNewBehavior(ApplicationConfiguration)} and
     * {@link #defaultOpenBehavior(ApplicationConfiguration)} methods to operate on the primary
     * model for the application. The primary model is either the only model, or the only model
     * with a {@link Model#primary()}} value of true
     *
     * @param applicationConfiguration The {@link ApplicationConfiguration}
     * @param consumer The routine to call on the model
     */
    private void withPrimaryModel(ApplicationConfiguration applicationConfiguration,  Consumer<Class<?>> consumer) {

        // See if we can find a primary model. This will either be the only model,
        // or the one marked primary on the Model annotation
        BeanDefinition primaryModel = applicationConfiguration.getPrimaryModel();

        // If we have a primary model, with a valid class name, then attempt to create
        // the model for that class.
        if (Objects.nonNull(primaryModel) &&
                Objects.nonNull(primaryModel.getBeanClassName()) &&
                !primaryModel.getBeanClassName().isBlank()) {
            try {
                final Class<?> modelClass = Class.forName(primaryModel.getBeanClassName());
                consumer.accept(modelClass);
            } catch (ClassNotFoundException e) {

                // This should never happen, but it keeps the compiler happy.
                log.error("Unable to open model because class {} was not found", primaryModel.getBeanClassName());
                throw new RuntimeException(e);
            }
        } else {
            log.warn( "No default model was found for the open. Annotate one class with @Model, or, if there are more than one models, with @Model(primary=true)" );
        }
    }


    @EnablerFor("cmdOpen")
    @SuppressWarnings("unused")
    public boolean defaultEnableOpenBehavior(ApplicationConfiguration applicationConfiguration) {
        // See if we can find a primary model. This will either be the only model,
        // or the one marked primary on the Model annotation
        BeanDefinition primaryModel = applicationConfiguration.getPrimaryModel();
        if (Objects.nonNull(primaryModel) &&
                Objects.nonNull(primaryModel.getBeanClassName()) &&
                !primaryModel.getBeanClassName().isBlank()) {
            try {

                // Is it one we could read?
                final Class<?> modelClass = Class.forName(primaryModel.getBeanClassName());
                return Serializable.class.isAssignableFrom(modelClass);
            } catch (ClassNotFoundException e) {

                // This should never happen, but it keeps the compiler happy.
                log.error("Unable to open model because class {} was not found", primaryModel.getBeanClassName());
                return false;
            }
        } else {
            return false;
        }
    }

    @EnablerFor("cmdNew")
    @SuppressWarnings("unused")
    public boolean defaultEnableNewBehavior(ApplicationConfiguration applicationConfiguration) {
        // See if we can find a primary model. This will either be the only model,
        // or the one marked primary on the Model annotation
        BeanDefinition primaryModel = applicationConfiguration.getPrimaryModel();
        return (Objects.nonNull(primaryModel) &&
                Objects.nonNull(primaryModel.getBeanClassName()) &&
                !primaryModel.getBeanClassName().isBlank());
    }



    @HandlerFor("cmdAbout")
    @SuppressWarnings("unused")
    public void defaultAboutBehavior(ApplicationConfiguration applicationConfiguration,
                                     ApplicationContext applicationContext) {

        // The default behavior is to look for a readable image file
        // with the name of the application at the root of the resource
        // files. The readable image files is obtained from the ImageIO
        // class. Once a file is found, it will be displayed in a dialog.
        // If no image with the name of the application is found, the
        // default image from "Spwing.png" will be used instead.
        try {
            JFrame frame = new JFrame("TitleLessJFrame");
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(Box.createVerticalStrut(30));
            final URL imageResource = this.getClass().getResource("Spwing.png");
            if (Objects.nonNull(imageResource)) {
                BufferedImage wPic = ImageIO.read(imageResource);
                JLabel wIcon = new JLabel(new ImageIcon(wPic));
                wIcon.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
                panel.add(wIcon);
            }

            panel.add(Box.createVerticalGlue());
            final JLabel appName = new JLabel(applicationConfiguration.getApplicationName());
            appName.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            panel.add(appName);
            panel.add(Box.createVerticalGlue());

            String copyright = applicationConfiguration.getApplicationAnnotation().copyrightStatement();
            if (!copyright.isBlank()) {
                final JLabel copyrightLabel = new JLabel(copyright);
                copyrightLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
                panel.add(copyrightLabel);
                panel.add(Box.createVerticalGlue());
            }

            final JLabel aboutSpwing = new JLabel(applicationContext.getMessage("AboutSpwing", null, Locale.getDefault()));
            aboutSpwing.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            panel.add(aboutSpwing);
            panel.add(Box.createVerticalStrut(30));
            frame.add(panel, BorderLayout.CENTER);
            frame.setUndecorated(true);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 200);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    frame.dispose();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Used to denote a menu item with no action
     */
    @HandlerFor("cmdNOP")
    @SuppressWarnings({"unused"})
    public void noActionHandler(ActionEvent event) {
        log.debug("This method intentionally left blank");
    }


    @EnablerFor("cmdClose")
    @SuppressWarnings("unused")
    public boolean defaultEnableCloseBehavior() {
        // Enable the default close if there are any windows that are not
        // already disposed in the window list
        return Objects.nonNull(spwing.getActiveModel());
    }



    @HandlerFor("cmdClose")
    @SuppressWarnings("unused")
    public boolean defaultCloseBehavior(@NonNull JFrame jFrame, @NonNull Window theWindow, @NonNull @Model Object theModel) {

        log.debug( "In default close behavior: frame = {}, window = {}", jFrame, WindowUtils.getWindowTitle(theWindow));
        boolean doClose = true;

        // If the save command is enabled, then attempt to fire it, expecting a boolean result.
        if (spwing.isCommandEnabled("cmdSave") && checkForSave(theWindow)) {

            // Attempt to save the file. If that works, then we can continue.
            doClose = spwing.fireCommandWithResult("cmdSave", Boolean.class, false);
        }

        // Close the window if everything is OK
        if (doClose) {
            theWindow.dispose();
        }
        return doClose;
    }



    /**
     * Check with the user whether to save the model before closing it.
     *
     * @param theWindow The window being closed
     * @return TRUE if the close can continue
     */
    private boolean checkForSave(@NonNull Window theWindow) {

        String title = WindowUtils.getWindowTitle(theWindow);
        final Locale defaultLocale = Locale.getDefault();
        return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(theWindow,
                messageSource.getMessage("prompt_SaveFile", new Object[]{title}, "prompt_SaveFile missing", defaultLocale),
                messageSource.getMessage("title_SaveFile", null, "title_SaveFile missing", defaultLocale),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
    }


    /**
     * Returns TRUE if the model is a {@link Saveable} that needs to be saved;
     * false otherwise.
     *
     * @param theModel The model
     * @return TRUE to enable save
     */
    @EnablerFor("cmdSave")
    @SuppressWarnings("unused")
    public boolean defaultEnableSaveBehavior(@Model Object theModel) {
        log.debug( "In default enable save behavior, model = {}", theModel );
        if (theModel instanceof Saveable saveable) {
            return saveable.needsSave();
        } else {
            return false;
        }
    }


    /**
     * The default save behavior looks for a model that has {@link Model#extensions()}
     * defined and is {@link Serializable}. If both of these conditions are true, the handler
     * will attempt to get the file from the model, and will prompt the user for
     * the file if necessary. Once the file is achieved, if it is not the same as
     * the file already in use for the model, the user will be prompted to see if
     * they want to override that file. If so, the handler will open the file for
     * write, and serialize the model to that file. This simple behavior works
     * for manu simple model instances.<br>
     * The default save behavior will look for the {@link Model#extensions()} array
     * to see what types to use. The first type will be used by default.
     * @param documentSession The {@link DocumentSession} instance containing information about the document
     * @return TRUE if the save was successful
     */
    @HandlerFor("cmdSave")
    @SuppressWarnings("unused")
    public boolean defaultSaveBehavior(@NonNull DocumentSession documentSession) {

        log.debug( "In default save behavior, document session = {}", documentSession);
        Object theModel = documentSession.getModel();

        // Make sure the model is file based and serializable
        if (theModel instanceof Serializable serializable) {

            // Get the file to write to. Validate that it is either new, the same file as the model
            // already has, or the user wants to override.
            //TODO Make sure this save doesn't override another open model file.
            File targetFile = getFilePromptingIfNecessary(documentSession);
            if (Objects.nonNull(targetFile) && (!targetFile.exists() || targetFile.equals(documentSession.getAssociatedFile()) || promptForOverride(targetFile))) {

                // Create a new file output and object output stream
                try (FileOutputStream fos = new FileOutputStream(targetFile);
                     ObjectOutput objectOutput = new ObjectOutputStream(fos)) {

                    // Notify we're going to write, write, save the file, and notify we wrote.
                    objectOutput.writeObject(serializable);
                    documentSession.setAssociatedFile(targetFile);

                    // TODO Need mechanism to clear dirty bit on model

                    // All good
                    return true;
                } catch (IOException e) {
                    log.error("Unable to save to file {}", targetFile.getName(), e);
                    spwing.postErrorDialog( "err_FileCannotBeWritten", targetFile.getName(), e );
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }


    /**
     * Check with the user that they want to overwrite the file
     *
     * @param targetFile The file that will be overwritten
     * @return TRUE to continue
     */
    private boolean promptForOverride(File targetFile) {

        Locale defaultLocale = Locale.getDefault();
        Object[] args = new Object[] { targetFile.getName() };
        String prompt = messageSource.getMessage("prompt_FileExists", args, "prompt_FileExists is missing", defaultLocale);
        String title = messageSource.getMessage("title_FileExists", null, "title_FileExists is missing", defaultLocale);

        return JOptionPane.showConfirmDialog(null, prompt, title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }


    /**
     * Get the file for this model. If the model already has a
     * file associated with it, return that. If not, prompt the
     * user for a file and save / return that.
     *
     * @return The file
     */
    private File getFilePromptingIfNecessary(@NonNull DocumentSession documentSession) {
        if (Objects.isNull(documentSession.getAssociatedFile())) {

            // Build a new file chooser
            JFileChooser fileChooser = new JFileChooser();

            // Get the file filters associated with this model class
            fileChooserUtils.buildFileFiltersForModel(fileChooser, documentSession.getModel());

            // Present the save dialog and get the option from the user
            int option = fileChooser.showSaveDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                return fileChooser.getSelectedFile();
            }
        }

        return documentSession.getAssociatedFile();
    }


    @HandlerFor("cmdQuit")
    @SuppressWarnings("unused")
    public void defaultQuitBehavior(Spwing spwing, QuitEvent event, QuitResponse response) {

        // We need to close any windows that haven't been closed already
        // The Window.getWindows() list may include windows that have been
        // closed / disposed already, so we need to filter out those that
        // are not displayable.
        List<Window> windows = Arrays.stream(Window.getWindows())
                .filter(Window::isDisplayable)
                .toList();

        // Close any open windows. If the close fails, then
        // we cancel the quit
        for (Window window : windows) {

            // By supplying the target window here, we override the
            // default window injected value (the front window) in the
            // invoker. Also, we get a boolean back from the command
            // to determine whether to continue or break this loop.
            boolean result = spwing.fireCommandWithResult("cmdClose", Boolean.class, false, window, response );
            if (!result) {
                response.cancelQuit();
                return;
            }
        }
        response.performQuit();
    }

}
