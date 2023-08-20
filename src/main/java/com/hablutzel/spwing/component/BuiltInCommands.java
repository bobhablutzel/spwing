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

import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.annotations.*;
import com.hablutzel.spwing.command.CommandAwareUndoManager;
import com.hablutzel.spwing.context.DocumentSession;
import com.hablutzel.spwing.invoke.ParameterResolution;
import com.hablutzel.spwing.model.ModelConfiguration;
import com.hablutzel.spwing.util.FileChooserUtils;
import com.hablutzel.spwing.util.WindowUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.desktop.QuitResponse;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


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
public final class BuiltInCommands implements MessageSourceAware, ApplicationContextAware {

    private final Spwing spwing;
    private final FileChooserUtils fileChooserUtils;
    private Class<?> primaryModelClass = null;

    @Setter
    private ApplicationContext applicationContext;

    @Setter
    private MessageSource messageSource;

    /**
     * The default open behavior attempts to open the "primary"
     * model, as defined by {@link #getPrimaryModelClass()}
     */
    @HandlerFor("cmdOpen" )
    @SuppressWarnings("unused")
    public void defaultOpenBehavior() {
        withPrimaryModel(spwing::openModel);
    }




    @HandlerFor("cmdUndo")
    @SuppressWarnings("unused")
    public void defaultUndoBehavior(final UndoManager undoManager) {
        undoManager.undo();
    }


    /**
     * Enables the Undo command if there are undoable edits that can be
     * undone. This also changes the menu item text for the undo
     * command to reflect the name of the undoable edit
     *
     * @param undoManager The {@link UndoManager}
     * @param menuItem The {@link JMenuItem} that displays cmdUndo
     * @return TRUE for enabling cmdUndo
     */
    @EnablerFor("cmdUndo")
    @SuppressWarnings("unused")
    public boolean defaultEnableUndoBehavior( @Nullable final UndoManager undoManager,
                                              @Nullable final JMenuItem menuItem ) {

        // Make sure we have an undo manager
        final boolean canUndo = null != undoManager && undoManager.canUndo();

        // If the menu item is available, change the text to match the undo item
        if (null != menuItem) {
            final String presentationString = canUndo ? undoManager.getUndoPresentationName() : "";
            menuItem.setText(applicationContext.getMessage("cmdUndo",
                    new Object[]{ presentationString },
                    Locale.getDefault()));
        }
        return canUndo;
    }


    /**
     * Default redo behavior. Delegates to the undo manager.
     * Note this assumes that the default enable redo behavior
     * has already run and validated the existance of the undo manager
     *
     * @param undoManager The current undo manager
     * @see #defaultEnableRedoBehavior(UndoManager, JMenuItem)
     */
    @HandlerFor("cmdRedo")
    @SuppressWarnings("unused")
    public void defaultRedoBehavior( final UndoManager undoManager ) {
        undoManager.redo();
    }


    /**
     * Enables the Redo command if there are undoable edits that can be
     * redone. This also changes the menu item text for the redo
     * command to reflect the name of the undoable edit
     *
     * @param undoManager The {@link UndoManager}
     * @param menuItem The {@link JMenuItem} that displays cmdRedo
     * @return TRUE for enabling cmdUndo
     */
    @EnablerFor("cmdRedo")
    @SuppressWarnings("unused")
    public boolean defaultEnableRedoBehavior( @Nullable final UndoManager undoManager,
                                              @Nullable final JMenuItem menuItem  ) {

        // See that we can redo
        final boolean canRedo = null != undoManager && undoManager.canRedo();

        // If the menu item exists, change the presentation string to represent the
        // command to be redone
        if (null != menuItem) {
            final String presentationString = canRedo ? undoManager.getRedoPresentationName() : "";
            menuItem.setText(applicationContext.getMessage("cmdRedo",
                    new Object[]{ presentationString },
                    Locale.getDefault()));
        }
        return canRedo;
    }



    /**
     * The default new behavior attempts to create a new "primary"
     * model, as defined by {@link #getPrimaryModelClass()}
     */
    @HandlerFor("cmdNew" )
    @SuppressWarnings("unused")
    public void defaultNewBehavior() {
        log.debug( "Executing default new behavior" );
        withPrimaryModel(spwing::newModel);
    }


    /**
     * withPrimaryModel is a helper function to find the primary model
     * and call a consumer function on that class.
     *
     * @param consumer The routine to call on the model
     * @see #getPrimaryModelClass()
     */
    private void withPrimaryModel( final Consumer<Class<?>> consumer) {

        Class<?> primaryModelClass = getPrimaryModelClass();
        if (null != primaryModelClass) {
            consumer.accept(primaryModelClass);
        }
    }


    /**
     * Attempt to figure out the "primary" model. This will be opened
     * by default if no other open/new functionality is provided. The
     * primary model is defined one of two ways:
     * <ul>
     *     <il>Through a bean named "primaryModel" defined at singleton
     *     scope. This should be a Class instance denoting the class of the
     *     primary model</il>
     *     <il>Through a scan for the {@link ModelConfiguration} bean classes.
     *     These beans will not be created, but the class itself will be enough
     *     to enumerate the potential models. If there is just one, that will be
     *     used as primary, otherwise one of these will be selected at random.</il>
     * </ul>
     * @return The (potentially null) primary class.
     */

    private Class<?> getPrimaryModelClass() {

        if (null == primaryModelClass) {

            // First test - see if there is a primaryModel bean defined.
            if (null != applicationContext && applicationContext.containsBean("primaryModel")) {
                Object primaryModelBean = applicationContext.getBean("primaryModel" );
                if (primaryModelBean instanceof Class<?> primaryModelBeanClass) {
                    this.primaryModelClass = primaryModelBeanClass;
                } else {
                    log.warn( "Bean primaryModel was defined, but is not a Class definition");
                }
            } else {

                // The configuration doesn't provide a primary model bean.
                // Scan for any known ModelConfiguration instances. If there is just one, it is
                // primary by default. If not, find the first that is marked as primary
                ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
                scanner.addIncludeFilter(new AssignableTypeFilter(ModelConfiguration.class));
                Set<BeanDefinition> beans = scanner.findCandidateComponents(spwing.getContextRoot().getPackageName());
                Set<Class<?>> models = beans.stream()
                        .map(this::configurationDefinitionToModelClass)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                if (models.size() > 1) {
                    log.warn("Multiple models are defined, but the configuration does not define a primary model");
                    log.warn("A random model will be used as primary");
                    log.warn("(You can do this be putting a \"primaryModel\" bean on the application configuration" );
                }

                if (!models.isEmpty()) {
                    this.primaryModelClass = models.iterator().next();
                } else {
                    log.warn( "Could not find any model configuration instances" );
                }
            }
        }
        return this.primaryModelClass;
    }

    /**
     * Attempt to convert a bean definition that has a
     * {@link ModelConfiguration} interface into the model class
     * that is being configured.
     *
     * @param beanDefinition The {@link BeanDefinition}
     * @return The class representing the generic model for the configuration class
     */
    private Class<?> configurationDefinitionToModelClass(final BeanDefinition beanDefinition) {
        try {

            // Get the bean class name as a resolvable type
            Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
            ResolvableType beanType = ResolvableType.forClass(beanClass);

            // This should define at least the ModelConfiguration interface or it wouldn't
            // have been captured. So get all the interfaces, and look for the ModelConfiguration
            ResolvableType modelConfigurationInterface = Arrays.stream(beanType.getInterfaces())
                    .filter(i -> null != i.getRawClass() && ModelConfiguration.class.isAssignableFrom(i.getRawClass()))
                    .findFirst()
                    .orElse(null);

            if (null != modelConfigurationInterface) {
                return modelConfigurationInterface.getGenerics()[0].getRawClass();
            } else {
                return null;
            }
        } catch (ClassNotFoundException e) {
            return null;
        }
    }


    /**
     * Enables the open command if there is a primary model with
     * defined file extensions. File extensions will be defined
     * as a bean by the model configuration class.
     * @return TRUE if open can be performed
     */
    @EnablerFor("cmdOpen")
    @SuppressWarnings("unused")
    public boolean defaultEnableOpenBehavior() {
        // See if we can find a primary model. This will either be the only model,
        // or the one marked primary on the Model annotation
        return null != getPrimaryModelClass() && !FileChooserUtils.getActiveFileExtensions(applicationContext).isEmpty();
    }


    /**
     * Enables the new command if there is defined primary model
     * that can be opened
     * @return TRUE if cmdNew should be enabled
     * @see #getPrimaryModelClass()
     */
    @EnablerFor("cmdNew")
    @SuppressWarnings("unused")
    public boolean defaultEnableNewBehavior() {
        return null != getPrimaryModelClass();
    }


    /**
     * Provides default About box behavior. This routine creates an about
     * with information about the application and displays it.
     * @param spwing The {@link Spwing} instance
     */
    @HandlerFor("cmdAbout")
    @SuppressWarnings("unused")
    public void defaultAboutBehavior(final Spwing spwing) {

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
            if (null != imageResource) {
                BufferedImage wPic = ImageIO.read(imageResource);
                JLabel wIcon = new JLabel(new ImageIcon(wPic));
                wIcon.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
                panel.add(wIcon);
            }

            panel.add(Box.createVerticalGlue());
            final JLabel appName = new JLabel(spwing.getApplicationName());
            appName.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            panel.add(appName);
            panel.add(Box.createVerticalGlue());

            if (applicationContext.containsBean("copyright" )) {
                String copyright = applicationContext.getBean("copyright", String.class);
                // TODO get a message instead
                if (!copyright.isBlank()) {
                    final JLabel copyrightLabel = new JLabel(copyright);
                    copyrightLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
                    panel.add(copyrightLabel);
                    panel.add(Box.createVerticalGlue());
                }
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
    public void noActionHandler() {
        log.debug("This method intentionally left blank");
    }


    /**
     * Enables the cmdClose if there is an active model
     * @param theModel The active root model object for the document
     * @return TRUE if the close command is enabled
     */
    @EnablerFor("cmdClose")
    @SuppressWarnings("unused")
    public boolean defaultEnableCloseBehavior( @Nullable @Model final Object theModel) {

        // Enable the default close if there is a model to close
        return null != theModel;
    }


    /**
     * The default close behavior. Attempts to save the document if needed
     * before closing the window.
     * @param jFrame The Swing {@link JFrame} instance
     * @param theWindow The associated AWS {@link Window} instance
     * @param theModel The root model object
     */
    @HandlerFor("cmdClose")
    @SuppressWarnings("unused")
    public void defaultCloseBehavior(final @NonNull JFrame jFrame,
                                     final @NonNull Window theWindow,
                                     final @NonNull @Model Object theModel) {

        log.debug( "In default close behavior: frame = {}, window = {}", jFrame, WindowUtils.getWindowTitle(theWindow));
        boolean doClose = true;

        // If the save command is enabled, then attempt to fire it, expecting a boolean result.
        if (spwing.isCommandEnabled("cmdSave") && checkForSave(theWindow)) {

            // Attempt to save the file. If that works, then we can continue.
            spwing.fireCommand("cmdSave");
        }

        // Close the window if everything is OK
        theWindow.dispose();


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
     * Returns TRUE if the model is a {@link Serializable} that the
     * document session considers to be dirty
     *
     * @param undoManager The undo manager
     * @param theModel The model
     * @return TRUE to enable save
     */
    @EnablerFor("cmdSave")
    @SuppressWarnings("unused")
    public boolean defaultEnableSaveBehavior(@Nullable final CommandAwareUndoManager undoManager,
                                             @Nullable final @Model Object theModel) {
        return  null != undoManager &&
                theModel instanceof Serializable &&
                !FileChooserUtils.getActiveFileExtensions(applicationContext).isEmpty() &&
                undoManager.changesHaveOccurredSinceLastCheckpoint();
    }


    /**
     * The default save behavior looks checks to see if the "fileExtension" bean
     * is present and the model is serializable. If both of these conditions are true, the handler
     * will attempt to get the file from the model, and will prompt the user for
     * the file if necessary. Once the file is achieved, if it is not the same as
     * the file already in use for the model, the user will be prompted to see if
     * they want to override that file. If so, the handler will open the file for
     * write, and serialize the model to that file. This simple behavior works
     * for manu simple model instances.<br>
     * The default save behavior will look for the fileExtension bean
     * to see what types to use. The first type will be used by default.
     * @param documentSession The {@link DocumentSession} instance containing information about the document
     * @param theModel The underlying model root object, which will be saved if needed
     * @param jFrame The view frame associated with the document (used to modify the title)
     */
    @HandlerFor("cmdSave")
    @SuppressWarnings("unused")
    public void defaultSaveBehavior(final DocumentSession documentSession,
                                       final @Model Object theModel,
                                       final @Nullable JFrame jFrame ) {

        // Get the list of extensions
        List<String> fileExtensions = FileChooserUtils.getActiveFileExtensions(applicationContext);

        // Make sure the model is file based and serializable
        if (theModel instanceof Serializable serializable && !fileExtensions.isEmpty()) {

            // Get the file to write to. Validate that it is either new, the same file as the model
            // already has, or the user wants to override.
            //TODO Make sure this save doesn't override another open model file.
            File targetFile = getFilePromptingIfNecessary(documentSession, fileExtensions);
            if (null != targetFile && (!targetFile.exists() || targetFile.equals(documentSession.getAssociatedFile()) || promptForOverride(targetFile))) {

                // Create a new file output and object output stream
                try (FileOutputStream fos = new FileOutputStream(targetFile);
                     ObjectOutput objectOutput = new ObjectOutputStream(fos)) {

                    // Notify we're going to write, write, save the file, and notify we wrote.
                    objectOutput.writeObject(serializable);
                    documentSession.setAssociatedFile(targetFile);

                    // Mark the last checkpoint on the undo stack, so we know if we need to save again
                    documentSession.getUndoManager().checkpoint();

                    // Change the window name
                    if (null != jFrame) {
                        jFrame.setTitle(targetFile.getName());
                    }

                } catch (IOException e) {
                    log.error("Unable to save to file {}", targetFile.getName(), e);
                    spwing.postErrorDialog("err_FileCannotBeWritten", targetFile.getName(), e);
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("User cancelled save" );
            }
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
    private File getFilePromptingIfNecessary(final DocumentSession documentSession,
                                             final List<String> fileExtensions) {

        if (null == documentSession.getAssociatedFile()) {

            // Build a new file chooser
            JFileChooser fileChooser = new JFileChooser();

            // Get the file filters associated with this model class
            fileChooserUtils.buildFileExtensionFilters(fileChooser, fileExtensions);

            // Present the save dialog and get the option from the user
            int option = fileChooser.showSaveDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                return fileChooser.getSelectedFile();
            }
        }

        return documentSession.getAssociatedFile();
    }


    /**
     * Default quit functionality. Attempts to close all the
     * open windows and then exits
     * @param spwing The {@link Spwing} instance
     * @param response The {@link QuitResponse} for cancelling the quit of the close fails
     */
    @HandlerFor("cmdQuit")
    @SuppressWarnings("unused")
    public void defaultQuitBehavior(final Spwing spwing,
                                    final QuitResponse response) {

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

            try {
                // By supplying the target window here, we override the
                // default window injected value (the front window) in the
                // invoker.
                spwing.fireCommand("cmdClose",
                        ParameterResolution.forClass(Window.class,window),
                        ParameterResolution.forClass(QuitResponse.class,response));
            } catch (RuntimeException e) {
                response.cancelQuit();
                return;
            }
        }
        response.performQuit();
    }

}
