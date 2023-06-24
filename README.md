<p style="text-align: center; font-size: 48px; font-style: italic">
Spwing
</p>

<div style="text-align: center;">
    <img src="https://upload.wikimedia.org/wikipedia/commons/6/61/Narcissus_icon.svg"
         alt="Spwing logo: A daffodil"
         height="100">
    <!-- Spwing logo attribution: CortoFrancese, CC BY-SA 4.0 <https://creativecommons.org/licenses/by-sa/4.0>, via Wikimedia Commons -->
</div>

<p style="text-align: center; font-size: 36px">
The <i><u>ridiculously</u></i> easy cross-platform GUI framework
</p>

Spwing provides a cross-platform application development framework that merges the wide
platform support of Java Swing with the powerful and intuitive programming style of Swing - 
and adds a few surprises of it's own.

NOTE: Spwing is a work in progress. Some features are incomplete and could change without
notice. However, the basic functionality works.

## Features

- Automatic multi-document support
- A preference for convention over configuration combined with simple configuration when convention doesn't work
- Annotation support for configuration
- Automatic integration for native look & feel
- Automatic support for saving and opening files
- Automatic platform-specific resource resolution
  - Sensitive to platform and version
  - Used for localization, views, and menus
- Descriptive language for JFrame descriptions
  - Replaces multiple lines of Java with one succinct, intuitive declaration
  - Includes flexible binding mechanisms to any model or controller
  - Extensible for custom components
- Extremely flexible method discovery and invocation
- Integrated AWT and Spring event processing
- Automatic configuration of command handlers and enablers
- Automatic detection and adapting to system dark mode / light mode (while running) (tested on Mac only)

## Roadmap
- Replace the current json based menu specification with a DSL
- Support all Swing visual components
  - (Currently supported: JLabel, JTextField, JRadioButton, JCheckBox, JButton)
- Additional binding implementations
- Splash screen
- Preferences dialog & storage
- Testing and support for Windows & Linux

## Spwing Hello World

The Spwing Hello World application has two functional lines; one to launch the application, and 
one to create a JLabel containing the phrase "Hello world".

```Java
package com.hablutzel.hello;

import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.annotations.AllInOneApplication;

import javax.swing.JLabel;
import java.awt.Component;

/**
 * Hello world! using Spwing
 *
 */
@AllInOneApplication(applicationName = "Hello World")
public class SpwingHelloWorld
{
    @SuppressWarnings("unused")
    public Component buildSwingComponents() {
        return new JLabel("Hello World");
    }

    public static void main( String[] args ) {
        Spwing.launch(SpwingHelloWorld.class);
    }
}

```

There are three key lines in this example:

```Java
@AllInOneApplication(applicationName = "Hello World")
```

This line identifies the class as an all-in-one Spwing application. The all-in-one
application is the simplest form of a Spwing application; in this case there is a 
single model and controller wrapped into a single class. In this case, the 
most obvious use of the annotation is to define the application name, but there
is some additional functionality involved (notably marking this class as the 
model and controller).


```Java
    public Component buildSwingComponents() {
        return new JLabel("Hello World");
    }
```

This routine creates and returns a Swing JLabel. Note that no frame or
window is necessary. While most applications will create these, if they are
not created the framework will them automatically.

The routine <code>buildSwingComponents()</code> is found by reflection. In this
simple case, it doesn't take any arguments, but part of the power of the Spwing
framework is that arguments will be automatically injected. More on that later.


```Java
    public static void main( String[] args ) {
        Spwing.launch(SpwingHelloWorld.class);
    }
```

This routine launches the Spwing framework. It takes a single class as an 
argument. This class, known as the *context root* is used to set up the 
environment, find key classes, etc. All the classes for the application should
be in the same package, or a child package, of the package for the context root.

The result of this code is a desktop application that displays a single window 
with the text "Hello World", allows for multiple windows to be opened / closed, 
automatically supports a full, native-like look and feel.


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

In addition to the documents, the Swping framework supports a separate application
class. This is typically used for common configurations (like a shared main menu)
but is optional in the framework; a Swping application can function with only 
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

When views are created, the elements can be *bound* to a model or controller field.
When bound, changes to the view are automatically pushed to the bound field. This 
ensures that the field always has the same value as the field.

In order to make the binding bidirectional, the field needs to emit an event when the
field is updated. The view element will automatically listen to this event (called a trigger)
and update as required. The framework automatically deals with cyclic updates, so the 
model is free to signal the event even if the view is the source of the change.

While the displayed value of the view is usually bound, any field of the view that 
follows Java Bean semantics can be bound. This makes it trivial to enable/disable
buttons, or do exotic things like changing the font color of a label based on model state.
The bindings can be full Flexpressions, so they can include conditional evaluations,
tri-state operatings, and the like.

Work in progress: *Groups* of view elements can be bound to a single model field.
This is designed to be used for UI elements such as radio buttons, combo boxes, 
and so forth. If all the view elements in a group are AbstractButton instances,
a ButtonGroup will automatically be created. 

## Events
Spwing supports both document events and AWT events. AWT events are automatically generated by 
Spwing components and include events such as mouse movement and button clicks.
Document events are generated by calling <code>DocumentEventPublisher.publish</code> 
and are generally used to signal changes to the model state.

In both cases (document and AWT events), the name of the event follows a convention.
For document events, the convention is that event names begin with "evt": ```evtNameChanged```.
For AWT events, the name is based on the method of the event passed to the AWT event
listener: ```mouseMoved```, ```actionPerformed```. There are two exceptions for AWT
events. 
- In addition to ```actionPerformed```, button action clicks are also called ```clicked```. 
The two can be used interchangeably
- ```HierarchyBoundsListener.ancestorMoved``` is called
```hierarchyAncestorMoved``` to disambiguate it from the same event signalled by 
the ```AncestorListener``` class

Events look for and invoke a method. This method will be automatically discovered;
there is no need to link the method to a listener. For document events, the method
name is the event with "evt" replaced by "on":

```Java
public void onNameChanged( ... ) {}
```

For AWT events, the name is based on the event and the view item:

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

Again, if methods are named by convention then **nothing** has be be done to done
to connect the event to the method. Also, because the method is called via an invoker,
the method can take any parameters that can be discovered at runtime. If the
naming convention doesn't work, the annotation ```@ListenerFor``` can explicitly
specify the event name. (In this case, the "evt" prefix must be included.)

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
Spwing has a build-in domain specific language for creating view files. The language
is declarative, allowing the specification of the view components, the binding for this
components, and other capabilities.

See SVWFViewFactory for more information


## Platform resources
The ```ResourceUtils``` service can load resources in a platform specific
manner. The service will first look for a resource with the platform name and
version, then the platform name, then just the base resource name. This 
is used throughout the framework and allows for platform-specific resource files.
