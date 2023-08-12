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

### Release Notes


#### Version 0.6.1
- Binding has been simplified in two ways.
  - In SVWF files, model properties can be bound to Swing component properties 
    directly at component definition time by using the bind operator '=>' instead
    of the assignment operator '=". For example, the following code binds the 
    JLabel text field to the "name" field of the model object.
  
        label: JTextField( text => "name" ); 

    This binding will be bidirectional if the property is writable, so changes
    to the text field will result in changes to the model
  
  - In order to allow for more complicated models, you can specify property paths
    in addition to simple property names

        label: JTextField( text => "person.name" ); 

    This represents a path from the root model object, to a field named ```person```, which
    itself represents a class with a field named ```name```. Since these are properties,
    the classes should implement a class that accepts ```PropertyChangeListener``` instances
    and reports that properties have changed. However, you don't want the embedded class to 
    know that it is part of the hierarchy, so it should just signal that the ```name``` property
    changed. Spwing will build a bridge from there to the ```person.name``` that needs to be
    signalled in order to trigger the changes to the Swing object. This happens automatically
    so long as both the leaf class and the root model class accept change listeners.
  - The path given above is simple, but more complex paths including collections, etc are possible.
    Just be aware that the path itself cannot change; in other words if you add or more items
    from the collection the path name may not be correct.

#### Version 0.6.0
Big changes in this version, hence the bump in version number. A lot
of the changes are intended to make the model class more Java generic.
- How models and controllers are located
  - First off, the old mechanism for identifying model and controller
  classes is no longer needed. Instead, the ```ResolvableType``` changes in
  0.5.2 opened the door to the ```ModelConfiguration<M>``` class, which 
  is loaded before the model class is created. The ```ModelConfiguration<M>```
  class, which should be "document" scoped, can have any model level 
  beans associated with it (with the caveat that this bean is created
  *before* the model or controller beans, and should therefore not 
  reference them). 
  - You do not have to create beans of type ```ModelConfiguration<M>``` (for
  example in the application configuration discussed below). Swping
  will scan for these classes and create them as needed.
  - If the ```ModelConfiguration<M>``` instance creates a bean of type
  ```ModelFactory<M>```, which M in both cases is the model class, that 
  factory will be used to create the model. If not, a model factory will
  be generated on the fly, looking for ```create``` and  ```open``` static
  methods on the model class. If *those* aren't available either, default
  functionality will be used instead.
  - Another useful model configuration bean is ```fileExtension``` (a String
  or collection of Strings). If present, these will inform Swping that the 
  model (which should be Serializable) is eligible for default file handling
  and, furthermore, the file extensions to associate with the model. File
  extensions are specified without the dot delimiter (i.e. "txt" not ".txt"). 
  You can specify a description of these file types by including a localization
  string "desc_EXT" in your application properties file (e.g. desc_txt).
  - If you need to configure beans at the application level, simply
  create a Swing Service or Component at "singleton" scope. One example
  of this supported in 0.6.0 is the ```primaryModel``` bean which, if present,
  denotes the default model for applications with multiple models. If
  an application only has a single model, this is not needed. However, for
  default new/open functionality to work, *at least one* of the ```primaryModel``` OR
  and a ```ModelConfiguration<M>``` class must exist.
  - While ```Model``` and ```Controller``` annotations are no longer needed,
  they can still be added to parameters that are invoked via ```Invoker```
  semantics (such as handlers and enablers). A parameter so annotated will
  get the current model or controller object, respectively.
- Undo/Redo support.
  - The undo/redo support for Spwing follows the model of "make it easy".
  Most of the processing happens behind the scenes, with only minimal
  effort needed by the programmer.
  - Changes that occur directly to Swing elements such as text fields
  get undo/redo processing automatically (so long as they are created with
  the ```ComponentFactory``` class, which the SVWF processor uses). 
  - Atomic changes (typing individual keys in a ```JTextField```) are 
  bundled in a meaningful way, so undo/redo will handle the entire typing
  sequence, not individual letters.
  - In some cases, elements in the view will change due to controller 
  actions (think about a button handler changing the text of a field). 
  If you use the ```ChangeCommand``` class (or a subclass) to encapsulate
  that change, you not only get undo/redo but the Swing events will 
  automatically and gracefully be elided so that the undo/redo stack
  is intuitive to the user. See the README_UNDO.md file for more info.
  - Undo/redo processing is at the document level, so each document (i.e.
  each model/controller pair) will have their own undo stack.
- File handling
  - The default file processing behavior now relies on the undo/redo
  stack to determine whether a document is dirty, rather than needing
  the document to track that independently.
  - The undo/redo stack is preserved across saves, so you can still
  undo a change after saving. This will make the document dirty again
  but will not undo the save. Saves cannot be undone.
  - With this change, the ```Saveable``` interface was no longer
  needed and was eliminated.
  - With this change, the ```BaseModel``` class was no longer useful
  and was eliminated.
- Trigger changes
  - Prior to 0.6.0, model objects were required to signal changes
  to their state through DocumentEvents (using DocumentEventPublisher).
  That is still supported. However, that mechanism is Spwing specific,
  so I wanted to have a mechanism that was more "vanilla" Java.
  - As of 0.6.0, if the model implements PropertyEditor, the framework
  will listen for PropertyChangeEvents and use those to trigger view
  updates. 
  - If the model does not implement PropertyEditor, but does declare
  a method ```addPropertyChangeListener``` that (a) is non-static,
  (b) is non-abstract, and (c) takes a single PropertyChangeListener
  parameter, then that mechanism will be used by the framework as well.
  - In order to enable the check for PropertyChangeEvent listeners,
  *do not declare* refresh triggers. In other words, instead of
  ```field.text => myProperty ["evtFieldChanged"]``` in the 
  SVWF file, use ```field.text => myProperty```. Since the event
  trigger list is empty, Spwing will attempt to use PropertyChangeEvent
  semantics instead. 
  - If you want a quick and dirty base class for signalling property events, see PropertyChangeModel
- Other minor changes:
  - Invoker changes: This is mostly framework internal, but the Invoker now uses a
  parameter resolution mechanism that allows parameter resolves to state that
  the value was resolved, but was null. This is different than the value was not resolved, and
  handles some conditions where the parameter can, in fact, be null.
  - The ```enable``` methods can now optionally accept a JMenuItem
  which will be the menu item associated with the command being 
  enabled. The enabler can then directly manipulate this item. One
  place this is used is in the undo/redo processing, where the enable
  method changes the menu text depending on the edit being undone/redone.
  - Component injection has been changed. Previously it only worked with
  SVWF based views, and injected into either the model or the controller.
  Now it works for any views (SVWF or Reflective) but no longer injects
  into the model (in the spirit of keeping the model strictly separate
  from Spring functionality).
  - Moving away from using Object.isNull and Object.nonNull in code
  - Lots of misc bug fixes.

#### Version 0.5.2
- Changed the Invoker class to use the Spring ResolvableType class
rather than raw Java classes. This opens the door to much better
generic parameter support. As of 0.5.2, if you have a parameter
that takes an instance of ```Generic<SomeClass>``` and a bean of
```Generic<SomeClass>``` as well as a bean of ```Generic<SomeOtherClass>```
the correct bean will be used. As an example of this, you can 
create beans of ```List<Person>```, ```List<Address>```, and 
```List<PhoneNumber>``` and Spwing will find the right bean to
use for parameter passing.
- The above also works for varargs and wildcards. The following
handler will be called with all the beans that are  ```List```s

```java
    void handleSomeCommand( final List<?>... allLists ) { ... }
```

- With the new Generics support enabled, rethought the relationship
between the model, controller, and view. The thinking is this:
  - Models are most likely to be reused from another purpose, and
    should not require much (if any) Spwing-specific capabilities.
    To accommodate this, removed the idea of a ```Model``` annotation
    on the model class (but left the ```Model``` annotation for parameters)
  - If the model is expected to be a POJO without Spwing modifications, then
    the controller becomes the main entry point for the 
    Spwing specific functionality. As before, the controller can be found
    in several ways:
    - New to 0.5.2, and highest priority, will be an attempt to find a bean
      that implements the ```ConfigurationFor<M>``` interface, where M is the 
      model class. This class allows the specification of the controller, file 
      extensions, and similar configuration items that were previously only available
      in the ```Model``` annotation.
    - Failing to find that class, and also new to 0.5.2, will be an attempt to find a bean
      that implements the ```ControllerFor<M>``` interface, where M is the model class.
    - The controller can be associated via naming convention; for a class
      named XXXModel, Spwing will look for a controller class named XXXController.
      This works well for models created from scratch.
    - As before, you can choose to annotate the model with a @Model annotation
      that specifies the controller class. 
    - Any of these classes can be annotated with the ```Controller``` annotation,
      although this becomes strictly optional in Spwing 0.5.2.
  - The framework now builds a ```ModelConfiguration``` instance prior to 
    opening the model; this will have information about the configuration
    of the model class. This will be built only once per model class, the first
    time the model is loaded. This class has the capability of building the 
    controller and view objects, contains the configuration information about
    the model (from whatever source), etc.
- Added support for JComboBox instances. *discuss binding to enums and lists*

#### Version 0.5.1
- Added support for gridBagLayout on SVWF layout specifications.
- Changed handling of SVWF constants. SVWF creates a set of constants
  (in the ```SVWFConstantsFactory``` class) that can be used when specifying
  the value in a key/value pair (such as component properties or, in 0.5.1,
  gridBagLayout properties). Examples of these SVWF constants are ```$BOTTOM```,
  which is equivalent to ```SwingConstants.BOTTOM```, and ```$HORIZONTAL```. The problem
  with ```$HORIZONTAL``` is that, in the Swing framework, there are multiple
  constants with that name but different values - ```SwingConstants.HORIZONTAL``` (==0) and 
  ```GridBagConstraints.HORIZONTAL``` (==2). The new ContextualConstant mechanism
  allows the SVWF parser to determine which to use contextually, and get the
  right value.
- Added a new SVWF pseudo view component: ```filler```. If you specify a 
  layout component named ```filler``` *and no such element exists*, a new  
  empty JPanel will be created on the fly. This is useful in layouts like the
  GridBagLayout for spacing out components on a row. 
- The new [SpwingGridBagDemo](https://github.com/bobhablutzel/SpwingGridBagDemo) demonstrates
new functionality in this release.

#### Version 0.5
Initial public release

