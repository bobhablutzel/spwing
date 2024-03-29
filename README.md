<p style="text-align: center; font-size: 48px; font-style: italic">
Spwing
</p>

<div style="text-align: center;">
    <img src="https://github.com/bobhablutzel/spwing/blob/main/src/main/resources/com/hablutzel/spwing/component/Spwing.png?raw=true"
         alt="Spwing logo: A daffodil"
         height="100">
    <!-- Spwing logo attribution: CortoFrancese, CC BY-SA 4.0 <https://creativecommons.org/licenses/by-sa/4.0>, via Wikimedia Commons -->
</div>

<p style="text-align: center; font-size: 36px">
The <i><u>ridiculously</u></i> easy cross-platform GUI framework
</p>

Spwing provides a cross-platform application development framework that merges the wide
platform support of Java Swing with the powerful and intuitive programming style of Spring - 
and adds a few surprises of it's own.

NOTE: Work on Spwing has been suspended as it never got much traction. The code remains in open source but not further updates are expected.

## Features

- A preference for convention over configuration combined with simple configuration when convention doesn't work
- Automatic multi-document support
- Annotation support for configuration
- Automatic integration for native look & feel
- Full undo/redo handling linked to the file save state
- Automatic support for saving and opening files
- Automatic platform-specific resource resolution
  - Sensitive to platform and version
  - Used for localization, views, and menus
- Descriptive language for Swing component descriptions
  - Replaces multiple lines of Java with one succinct, intuitive declaration
  - Includes flexible bidirectional binding mechanisms to any model or controller, even in hierarchies
  - Extensible for custom components
- Extremely flexible method discovery and invocation
- Integrated AWT and Spring event processing
- Automatic configuration of command handlers and enablers
- Automatic detection and adapting to system dark mode / light mode (while running) (tested on Mac only)

## Roadmap
- Replace the current json based menu specification with a DSL
- Support all Swing visual components
  - Currently supported: ImageIcons, Buttons, CheckBoxes, ComboBoxes, Date Spinners, Formatted Text fields,
    Labels, List Spinners, Number Spinners, Password Text fields, Radio Buttons, Text Fields
- Splash screen
- Preferences dialog & storage
- Testing and support for Windows & Linux

## Obtaining

Spwing is available on major Maven repositories. The current
version is 0.6.2.

```xml
        <dependency>
            <groupId>com.hablutzel.spwing</groupId>
            <artifactId>spwing</artifactId>
            <version>0.6.2</version>
        </dependency>
```
## Demos

The Spwing project aims to build out a robust set of demonstration applications, both to demonstrate
specific techniques and as a tutorial on using the framework. The demos build in complexity; the first
demos are fairly simple but demonstrate key capabilities.

All the demos automatically have the ability to open multiple document windows, save the contents
of the document, post event boxes, use menus, adapt to the look and feel of the platform, and so
forth. That's tables stakes for the framework.

- [SpwingLabelButtonDemo](https://github.com/bobhablutzel/SpwingLabelButtonDemo): A simple
application with a label that displays the value of a model element. Demonstrates
the basic components of Spwing (Application, Model, Controller, View) and introduces
the SVWF view layout language. Demonstrates unidirectional binding of a Swing component
to the value of a model element. Demonstrates how handler methods are associated with 
view components and how the model can be passed to handler methods. *Requires Spwing 0.6.0 or later*


    The SpwingLabelButtonDemo is discussed in detail below.
  
- [SpwingBoundTextFieldDemo](https://github.com/bobhablutzel/SpwingBoundTextFieldDemo): Builds
on the SpwingLabelButtonDemo, adding bi-directional binding of view elements to model
properties. In this demo you can update the model text field by typing or by button, and
see that the model element value actually changes. Shows how to arrange buttons on the 
right of a window with expected spacing. Also introduces localization of messages
and a common technique for associating controllers with models. *Requires Spwing 0.5 or later*
- [SpwingGridBagDemo](https://github.com/bobhablutzel/SpwingGridBagDemo): Essentially
the same as the [SpwingBoundTextFieldDemo](https://github.com/bobhablutzel/SpwingBoundTextFieldDemo),
except using a GridBagLayout instead of a BoxLayout. GridBagLayouts are powerful,
but take a lot of code to set up. Spwing provides a declarative approach to GridBagLayouts
resulting in a much more concise implementation. *Requires Spwing 0.5.1 or later*

## The Spwing approach

Spwing is designed to open multiple *documents*, each of which is a given 
a unique set of supporting infrastructure classes. If you are familiar with the
Spring web context, the Spwing document context is very similar. In fact, you can
mark Swing beans as having <code>@Scope("document")</code> and they will automatically
act as a singleton for each document. Each document will have their own instance, 
separate from all other documents.

The framework can open multiple documents, and can support documents of multiple
types. The plumbing of knowing which document is active is hidden from the 
application programmer, but can be accessed at any time. 

In addition to the documents, the Spwing framework supports a separate application
class. This is typically used for common configurations (like a shared main menu)
but is optional in the framework; a Spwing application can function with only 
document classes defined.

Spwing documents are based on the MVC (Model-View-Controller) paradigm, though perhaps
calling it a MCV system is more accurate. The framework also supports an 
application class, which can be used to support shared capabilities (usually
the main menu, though the model / controller can override that).

The *model* is the data container. The model is any object that supports
Java bean semantics; no super class or interface is required. [The framework does 
supply a BaseModel class that implements some commonly needed functionality,
but usage is completely optional.] The model is the first object 
created for a document, and triggers the creation of the controller and then
the view. 

The *controller* acts as the bridge between the view and the model. The controller
understands activities that happen due to the user manipulating the user interface, 
such as a button click or menu selection. [The functionality of the controller
is far simpler that many native Swing applications, because (as discussed below)
many of the housekeeping functionality that has to be manually coded in native
Swing is handled by convention or configuration in the framework.] Like the model 
class, the controller class can be any Java bean; no particular superclass or
interface is needed.

The *view* is the set of Swing classes that present the document to the user. 
These classes are generally free of application logic, and communicate to the 
controller or model via <code>bindings</code>, <code>events</code>, and 
<code>handlers</code>.

[Note: The goal of the Spwing framework is to support all Swing components, although
as this is written only a subset are supported.]

By convention, the model classes end in "Model" and the controller classes in "Controller".
For example, <code>TextModel</code> and <code>TextController</code>. If this convention
is followed, the controller can automatically be found from the model classes; if not
the controller can be specified via an annotation. For simple applications, or 
porting legacy code, the model and controller can be the same class. [Spwing supports
the notion of an all-in-one application where the application, model and controller are
all one class. That was used in the HelloWorld example above.]

## Example Application
The following example application is hosted at [SpwingLabelButtonDemo](https://github.com/bobhablutzel/SpwingLabelButtonDemo.git).
It displays a window with a label and button; when you click the button the label changes. This demonstrates
the view creation and binding features of the framework. This demo is intentionally stripped down, so does 
not demonstrate custom menus, command handler implementations, event processing (other than signalling a property change),
file handling, undo processing, platform specific resources, document beans, or other key features.

The basic Spwing application has four components: the application, the model, the view, and the controller.
The application is created once at startup time. The model, view, and controller are created once for
each document that is opened. This generally corresponds to a window or a file in the multi-window document
paradigm.

The main application code has the responsibility of initializing the framework. 
It does this by passing the ```contextRoot```, which is a class (usually the application
class itself, as in this example) that is in a package at the root of the package
hierarchy for the application. The context root tells the framework where and how
to find the other classes.

### The application class

```Java
package com.hablutzel.spwingDemo;


import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.annotations.Application;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


/**
 * The demo application. This launches the Spwing
 * environment, passing the application class as
 * the context root. This root will be used to find
 * all the key objects - they will be in the same
 * package or a subpackage.
 */
@Service
@Scope("singleton")
public class SpwingLabelButtonDemo {

  public static void main(String[] args) {

    // Launch Spwing
    Spwing.launch(SpwingLabelButtonDemo.class);
  }

  @Bean
  @Scope("singleton")
  public String launchCommand() {
    return "cmdNew";
  }

}

```

The application is a singleton Spring service. As a Spring
service, it can create beans as demonstrated wtih the ```launchCommand```
method. Spwing uses beans created by the application (and
later by the ```ModelConfiguration```) to control certain aspects
of execution. In this case, the ```launchCommand``` bean provides the
command - which is a string associated with a menu item - to fire 
when the application starts.

Note that the demo application does not define a menu bar. Spwing
defines a default one that includes the New command ("cmdNew"). You
can override this menu at the application or document level, but for
this simple application the default suffices. Spwing also defines
some built in functionality associated with common commands. So ```cmdNew```
is handled automatically by the framework to create a new document, ```cmdClose```
closes the current window, ```cmdAbout``` displays an About box, etc.
All of these default applications can be simply overridden just by 
declaring routines that handle those commands.

In the demo application, the application context root is the application 
and the application contains the ```main``` method. Neither of those is a requirement.

### The model class
The model class describes the data in the MVC architecture. The model
contains the state for the document; each document has its own copy
of the model object and therefore its own state. 

Spwing attempts to keep as much framework functionality out of the model
class as possible; put another way you should be able to take a Spwing
model and use it without change outside the Spwing framework. In fact, the 
only requirement is that the model has some way of signalling that properties
have changed (so that the views can be kept in sync). Spwing supports its own
```DocumentEvent``` structure based on the Spring ```ApplicationEvent```, but more commonly
models will implement ```PropertyChangeListeners``` based on the java.bean standard.
This can be done by inheriting from ```PropertyEditor```, inheriting from the Spwing
class ```PropertyChangeModel```, or simply declare a routine  ```addPropertyChangeListener```
that takes a ```PropertyChangeListener``` instance, either directly or via inheritance.

In this demo, the model is defined as a Spring service at "document" scope. The "document"
scope is defined by the Spwing framework and contains all the state for a given document.
Spwing supports a ```ModelConfiguration``` interface that can declare the model bean without
having to put Spring annotations on it, but this is outside the scope of this simple demo.

```Java
package com.hablutzel.spwingDemo;


import com.hablutzel.spwing.annotations.Model;
import com.hablutzel.spwing.model.ModelConfiguration;
import com.hablutzel.spwing.model.PropertyChangeModel;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The main model for the demo. The model contains a
 * single field ({@link #textField}, which contains
 * a string. The field is bound to the label created by
 * the view, so that changes to the model are reflected
 * in the view automatically. <br>
 * Spwing supports automatic saving and opening for
 * models that implement serialization. The {@link Model}
 * annotation gives the model an opportunity to define the
 * file extension of the files. By doing this, the application
 * can automatically detect when the model changes and
 * save the file as necessary.
 */
@Service
@Scope("document")
public class SpwingLabelButtonDemoModel extends PropertyChangeModel
                                        implements ModelConfiguration<SpwingLabelButtonDemoModel> {

    /**
     * The data for this model - a simple text field.
     */
    @Getter
    private String textField = "Hello World";


    /**
     * The setter of the field is augmented to signal
     * a document event when the text field changes. The
     * model signals this by noting that the state changed,
     * with the event to signal. This is functionality inherited
     * from the {@link PropertyChangeModel} class. Inheriting from this
     * class is <b>not</b> required by the framework, but it
     * does provide some useful functionality such as the
     * {@link PropertyChangeModel#signalChange(String, Object, Object)}
     *
     * @param textField The new value of the field
     */
    public void setTextField(String textField) {

        // Save the new value, and signal the change
        // This is sufficient for the bound label to be
        // updated in the view.
        String oldValue = this.textField;
        this.textField = textField;
        this.signalChange("textField", oldValue, textField );
    }
}
```

As mentioned in the code comments, the state for the model is held 
in the ```textField``` member. This example uses the ```PropertyChangeModel```
as a base. This is not required by the framework, but provides a helper
function ```signalChange``` which can be called when the state of a field
changes. The ```signalChange``` method signals a property state change, which
in turn triggers updates to the bound view objects.


### The view file
Spwing uses a declarative language to define the views. This further enforces
the separation of concerns between the model, view, and controller but also provides
a simplified syntax designed to make defining complicated view structures easy. The
view language is held in a .svwf (Spwing VieW File) file. You can, of course, still use
direct Swing code if you prefer, though (again) this simple demo does not illustrate that.

The svwf file is associated with the model by name - for the model SpwingLabelButtonDemoModel, 
the view file is SpwingLabelButtonDemoView.svwf. For classes that do not follow the 
convention of ending with "Model", the model class name itself will also be used to find the file.

```java
/* Define the components of our view
 *
 * - A Frame that contains the elements
 * - A label displaying text from the model
 * - A button to change the text in the model
 */
components {
    frame: JFrame(preferredSize=(400, 200));
    label: JLabel( text => "textField"); // Binds to the model property "textField".
    button: JButton(text="Click me");
}

// Layout the frame, with the label in the center and the button at the bottom
layout {
    frame: borderLayout(center=label, south=button);
}

```

The view file contains definitions for components, how they bind to the model, and how
they are laid out. This demo has three components: a frame, a label, and a button. Components
have names; these are used for event processing and can also be used to inject the component
into the controller if needed. The properties of the components can be set by name, and
as the ```horizontalAlignment``` property demonstrates Spwing defines some constants as well.
The view definition language also allows for the setting of defaults by component type, 
defining custom colors, font, etc.

This demo uses inline binding - the text of the label is associated with the model property
```textField```. Any changes to the model property will automatically be reflected in the label.
Since Labels are not editable, this is a unidirectional binding, but Spwing supports bidirectional
for any Swing components whose values can change. Spwing also supports property hierarchies 
("person.name" is a valid bidirectional binding). In more advanced use cases, Spwing can 
bind to expressions ("person.name.toLowerCase() + ':' + thing"), and can bind groups of components
(such as a set of radio buttons) to a single value.

The button component defines a push button control. The logic for the button control is 
handled in the controller, discussed below.

Finally, the ```layout``` clause shows how it all fits together. Spwing supports a variety of
layouts including Box, Border, Flow, GridBag and Button Bars.

### The controller class
The controller class has the responsibility of reacting to user actions. It 
can listen for events coming from the Swing components, menu items, and the like.
This functionality *could* be implemented directly in the model as well (the framework
doesn't care) but conceptually it is useful to have it in a separate class.

The controller is a Spwing specific component, and is associated with the model
through the ```ControllerFor``` generic interface. 

In this demo, the controller defines a single method: ```onButton_Clicked```. This
is associated with handling the button actions through convention: the method name 
denotes the view component (```button```) and action (```clicked```) that the method
handles. If you follow this convention, nothing more needs be done to connect the 
method with the view component. 

Spwing supports extremely flexible parameter definition for event handles (also for
command handlers and enablers). Basically, if Spwing can figure out a reasonable 
value for the parameter, it will supply it. In this case, the routine takes a model
object; since there is just one model for each document and the controller lives in the 
same document data space, that model is automatically provided. It must be stressed that
the parameters for the handler *are not defined by the framework*. The user defines what
parameters are needed; these can include beans, framework artifacts (such as the Spwing
instance), environment variables, and a host of other things. The ```Invoker``` class
is responsible for this bit of Spwing magic. See more on that below.


```Java
package com.hablutzel.spwingDemo;


import com.hablutzel.spwing.model.ControllerFor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


/**
 * The controller class has the responsibility of
 * reacting to view events. Controllers are mostly
 * used to handle button clicks and the like; they
 * are not needed for reflecting changes to bound
 * values in the model.<br>
 * For this demo, the button reacts by changing
 * the text of the model.
 */
@Service
@Scope("document")
public class SpwingLabelButtonDemoController implements ControllerFor<SpwingLabelButtonDemoModel> {

  /**
   * Handle the button click. Note that no wiring
   * is required for this method; the name of the
   * method defines the purpose through convention
   * and the arguments are automatically provided
   * by the framework.<br>
   * Specifically, this method is the handler for
   * an AWT event. The event is "Clicked", which is
   * an alias for the native "actionPerformed" event.
   * The method is identified as an event handler
   * because the name of the method begins with "on".
   * It is identified as being a handler for a specific
   * view component because the name of the component
   * (from the SVWF file) comes before the name of the
   * event, separated by an underscore. The rest of
   * the name "Clicked" defines the event to listen for.
   * (In order to keep more natural camelCase syntax, the
   * name could be "onButton_Clicked" or "onButton_clicked").<br>
   * The model parameter is the currently active model.
   * The Spwing framework will associate this automatically
   * with the active model by class matching - the model has the
   * class {@link SpwingLabelButtonDemoModel}. The parameter
   * is optional, but is needed in this case. The parameter list
   * is very flexible for event methods; see the Spwing
   * documentation for more details.<br>
   * Since the compiler doesn't see that the method is
   * called (it is called via reflection), we suppress the
   * unused warnings.<br>     *
   * @param model The active model.
   */
  @SuppressWarnings("unused")
  public void onButton_Clicked( SpwingLabelButtonDemoModel model) {

    // Change the text for the model
    model.setTextField("Button was clicked");
  }
}

```


### Other project files

Spwing supports application property files, with the same name as the 
application class (so in this case SpringLabelButtonDemo.properties). 
The framework supports both Locale and platform-specific resources; you can 
have a different set of resources for Windows, Linux and Mac if that's desired.
(The same applies to the SVWF files - those too can be platform specific).

For this demo app, the property file is simple: it just defines the name
of the application for the About box:

```java
applicationName=Spwing Button Label Demo
```

This is a maven project, so it has a pom.xml file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.hablutzel</groupId>
    <artifactId>SpwingLabelButtonDemo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>20</maven.compiler.source>
        <maven.compiler.target>20</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.hablutzel.spwing</groupId>
            <artifactId>spwing</artifactId>
            <version>0.6.2</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.28</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

</project>
```

That's it. Run the application and see the window. Click the button and see the bindings in action.
Try some of the default actions, such as About, New, and Close.

## Invokers
Spwing allows the programmer to declare methods to invoke for various activities. For
example, events and handlers (discussed below) point to methods on the model or controller
classes. These methods are found by name, and invoked at the appropriate time.

One of the most powerful features of Spwing is that the methods do not have to match
any particular signature. Instead, the signature of the method is evaluated dynamically with
the Spwing framework attempting to find reasonable values to pass in for every parameter.
This allows for extremely flexible and intuitive wiring of event listener and command handlers,
and allows for environment variables being passed directly to those methods instead of 
having to be stored in the model or controller as fields.

There are several ways parameters are matched:
- *By annotation*. Parameters that are annotated with ```@Model``` will get the
current model object; parameters annotated with ```@Controller``` will be given
the current controller object; and parameters annotated with ```@Application```
the current application object. Of course, the parameter type has to be compatible, but
does not have to be exact. Parameters annotated this way can be specified as ```Object```
instances, enabling generic processing.
- *By value*. The framework supports parameters annotated with ```@Value('expression')```.
For these parameters the expression will be evaluated as a *Flexpression* (see more about
flexpressions below).
- *By type*. The parameters can be specified by type, in this case the bean matching that 
type will be used as the parameter. If the argument is vararg, all beans of the type 
will be supplied.
- *By name*. If the parameter name can be discovered (i.e. the code is compiled with
```-debug``` or ```-parameters```) and a bean with the name can be found that matches
the type of the parameter, that bean will be used.

## Bindings

When views are created, the properties of the view elements can be *bound* to a model or controller field.
When bound, changes to the view are automatically pushed to the bound field. This 
ensures that the field always has the same value as the field.

In order to make the binding bidirectional, the field needs to emit a property change event when the
field is updated. The view element will automatically listen to this event (called a trigger)
and update as required. The framework automatically deals with cyclic updates, so the 
model is free to signal the event even if the view is the source of the change. The model classes
need to accept PropertyChangeListener instances. They can do this by subclassing the PropertyModel
Spwing class, or just by having a public non-static method "addChangeListener" that accepts
a ```PropertyChangeListener```. 

Each model class signals changes for it's own fields. For simple classes, the trigger name is
the same as the name of the field. For more complex hierarchies, the trigger name is the property
path to the bound field: ```person.name```. Spwing will automatically bridge between the signalled 
property in the person class (```name```) and the binding trigger (```person.name```).

While the displayed value of the view is usually bound, any field of the view that 
follows Java Bean semantics can be bound. This makes it trivial to enable/disable
buttons, or do exotic things like changing the font color of a label based on model state.
The bindings can be full Flexpressions, so they can include conditional evaluations,
tri-state operations, and the like.

For most properties, it's most convenient to bind the property using the inline property
operator ```=>``` when defining the component:

```java
    checkBox: JCheckBox( text="Checkbox", selected => "onOff" );
    textField: JTextField( text => "person.name" );
```

In this case, the properties will be based on the root model object. If you want to 
do something more complex, such as binding to an expression that is dependent on multiple
properties, you can use a bind statement after the components are defined:
```java
bind {
        textField.text => "person.name.toLowerCase() + ':' + person.age" ["person.name", "person.age"];
}

```
In this case, the textField ```text``` property is bound to the result of an expression that 
takes the person.name property, lower cases, and appends the current age. The value of the text
field will change if either the person.name or the person.age values change.

Expression bindings are unidirectional from the model to the view.


Work in progress: *Groups* of view elements can be bound to a single model field.
This is designed to be used for UI elements such as radio buttons, 
and so forth. If all the view elements in a group are AbstractButton instances,
a ButtonGroup will automatically be created. For these cases, in addition to 
creating the bindings you also create a new element that can be added to layouts:
```java
    bind {
        group:{ button1, button2 }.selected => "thing";
    }
```

## Events
Spwing supports both property events and AWT events. AWT events are automatically generated by 
Spwing components and include events such as mouse movement and button clicks.
Property events are associated with PropertyChangeListener instances and are generally
used for model state changes. 

If you are using binding, then you don't have to worry about property change events
in most cases. The framework will take care of it for you. 

For AWT events, you can create methods in your controller (or model, but don't do that) that
automatically are called when the event is signalled. Since binding takes care of the property
change events, this is usually reserved for things like reacting to buttons being clicked, etc.
Spwing defines an event based on the name of the method in the AWT event listener that would 
have been called, and looks for a handler method for that event.

For AWT events, you can create a method in the controller class (or the model, but don't do that) 
that is based on name of the method of the event passed to the AWT event. Take for example
a mouse moved event. This would be handled by a method ```MouseListener.mouseMoved()```, so for
Spwing the base event name is 'mouseMoved'. This base name is then used to find a handler
method as described below. There are only a couple exceptions to this convention:
- In addition to ```actionPerformed```, button action clicks are also called ```clicked```. 
The two can be used interchangeably
- ```HierarchyBoundsListener.ancestorMoved``` is called
```hierarchyAncestorMoved``` to disambiguate it from the same event signalled by 
the ```AncestorListener``` class

Once Spwing sees the AWT event base name, it looks for a handler method to call.
This method will be automatically discovered;
there is no need to link the method to a listener. The name is based on the event and the view item. 
If you have a SVWF definition

```java
            button: JButton();
```

that the user clicks in, Spwing will look for and call a method in your controller:

```Java
public void onOKButton_Clicked( ... ) {}
```

If you want to get the event for all active components, you can omit the 
component name:


```Java
public void onMouseMoved( ... ) {}
```

Event methods follow normal Java camel case conventions, but are case insensitive
with the first letter - onMouseMoved will match the mouseMoved event, but not the
mousemoved event.

Again, if methods are named by convention then **nothing** has be be done 
to connect the event to the method. Also, because the method is called via an Invoker,
the method can take any parameters that can be discovered at runtime. If the
naming convention doesn't work, the annotation ```@ListenerFor``` can explicitly
specify the event name and target (if any). The two definitions below result in the 
same behavior; the first is by convention and the second by annotation.

```java
    public void onButton_Clicked(...) {
```
```java
    @ListenerFor(target = "button", event="clicked")
    public void doSomething(...) {
```

Event listeners are automatically linked to the active document.

## Handlers and Enablers
Handlers react to menu selections. Menu items are associated with "commands", which 
by convention are strings starting with "cmd". Every command must have a handler, and optionally
can have an enabler.

Handlers provide the functionality of the menu, and are often methods on the controller
object. The handler name is derived from the command name: for the command "cmdOpen", the
handler will be ```handleOpen```. If this naming convention doesn't work, the ```@HandlerFor```
annotation can explicitly link the method to a command.

Handlers can take any parameters that can be derived at runtime. A real common dynamically
derived parameter is the model (via the class of the model, or the ```@Model``` annotation).
Handlers generally return null, but can also return a ```Runnable``` instance, 
which will be executed via ```Swing.executeLater```

If the command is associated with a handler but not an enabler, it will always be active
in the menu bar. If you need more control, you can specify an enabler method, which returns
a boolean to enable/disable the menu item. By convention, the enabler methods names
match the handler names but start with "enable" rather than "handle": ```enableOpen```. 
The ```@EnablerFor``` annotation can also be used.

Handler and Enablers are tied to the currently active document automatically.

The framework provides built in command handlers for a number of common functions:
- cmdNOP: Do nothing
- cmdSave: Save the model to a file if possible
- cmdClose: Close the active window
- cmdQuit: Close all open window and exit
- cmdNew: Create a new instance of the primary model
- cmdOpen: Read the primary model from a file if possible

## File handling
The framework can automatically save models that implement ```Serializable```
(or, equivalently ```Externalizable```). The model needs to provide the
file extension in the ```@Model``` annotation. The model can of course
implement more complex functionality as desired.

The framework currently uses an interface ```Saveable``` to determine
if the model needs to be saved. This interface is expected to be replaced
with a less invasive mechanism in the future, but Saveable will continue
to be supported.

## Menus
The application, model, or controller can provide a menu bar description. If none 
of these objects do, the framework will provide a minimal base menu item. See the 
```@MenuSource``` annotation for more information.

NOTE: Menus are currently specified in JSON. This will be continue to be supported
in the future, but a more flexible and power DSL is in the works.

## Flexpressions
Spwing unifies the idea of property paths and SPEL expressions into a single "flexpression".
If the expression can be interpreted as a property path (by default on the model object),
it will be taken as a property path. Alternatively, if the expression is of the form
```#{spel}```, then the ```spel``` expression will be evaluated as a SPEL expression. Finally,
if the expression is of the form ```${property}```, the ```property``` will be evaluated as
an application property. Flexpressions are used with bindings and ```@Value``` annotations

## SVWF
Spwing has a built-in domain specific language for creating view files. The language
is declarative, allowing the specification of the view components, the binding for this
components, and other capabilities.

See SVWFViewFactory for more information

### Undo handling
Spwing defines an ```UndoHandler``` for each open document. You can use this to automatically
get undo/redo handling:

```java
        final PropertyChangeCommand<String> propertyChangeCommand = new PropertyChangeCommand<>("New name", model.getPerson()::getName, model.getPerson()::setName, cmdChangeName);
        undoManager.addEdit(propertyChangeCommand);
```

The ```PropertyChangeCommand``` class is a subclass of ```ChangeCommand``` which is specialized for changing
properties that have setters and getters. ```ChangeCommand``` can be overridden directly for different
use cases.

In addition to undo/redo handling, the undo manager is used to determine if a document is "dirty" and needs
to be saved. You can still undo after saving (though that will make the document dirty again). The UndoManager
is a document-scoped bean, and so is available to any routine (such as handlers, listener, etc) that is
invoked via the Invoker framework.

See README_UNDO.md for more information.


## Platform resources
The ```ResourceUtils``` service can load resources in a platform specific
manner. The service will first look for a resource with the platform name and
version, then the platform name, then just the base resource name. This 
is used throughout the framework and allows for platform-specific resource files.
