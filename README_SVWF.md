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
The SVWF Language Reference
</p>

Spwing relies on Java Swing components for the views in the Model - Controller - View architecture.
Swing provides a robust set of components, but unfortunately no standard declarative syntax for
creating them at runtime. This leads to the use of UI builders or custom Java code that can be 
hard to write and maintain.

Spwing addresses this through a domain specific language - the Spwing VieW Factory language (SVWF), 
SVWF files describe the view in simple declarations about the components of the view, how they are
arranged, and how they map to the underlying model. Through the SVWF file, you can quickly describe
your user interface while still maintaining the ability to drop down into pure Java code if you need
fine grained control.

This document describes the SVWF language.

## Overview
The SVWF language consists of statements that 
- Describe a visual component
- Arrange visual components in a hierarchy
- Bind visual elements to the model (bidirectionally)
- Extend the set of components
- Define default attributes for components
- Define colors, images, and other complex values


## Components
Swing visual elements are considered to be *components* in the view description.
These components generally map to a descendent of the java.awt.Component class. 
The language does not generally enforce that, but some parts of the language (bindings,
layouts) might not work for items that are not components.

Components are defined through the ```components``` clause. 

```agsl
components {
    logo: ImageIcon( image="Spwing.png" );
    mainWindow: JFrame( title="Hello world", preferredSize=(300, 400));
    imageField: JLabel( text="<== Image here");
    textField2: JLabel( text="label" );
    textField1: JTextField( text="Hello world!", locale="en_US",
                        visible=true, toolTipText="Tooltip text!",
                        preferredSize=(400, 80) );
    button: JButton( text="Click Me!" );
    checkBox: JCheckBox( text="Checkbox" );
    panel: JPanel();
    thing1: JRadioButton( text="Thing 1", value="Thing1" );
    thing2: JRadioButton( text="Thing 2", value="Thing2" );
}
```

The ```components``` clause contains one or more component definitions. Each
definition starts with the name of the component (logo, mainWindow, imageField, etc).
This name will be used to refer to the component throughout the rest of the 
file and is required. Names follow standard Java identifier syntax - must start
with a letter, may contain numbers and underscores, etc. By convention names
are in camelCase.

Following the name, the component type is defined. The component type is an
*alias* to the actual class type. Aliases are defined for the standard Swing components
as the simple class name of the component: JFrame, JLabel, etc. See the ```invoke```
statement below for how you can define your own custom class aliases.

Following the type of the component are a set of name-value pairs. With a few
exceptions noted below, the names of these name-value pairs correspond to the 
properties of the Swing component when considered as a Java bean. For example,
the JLabel class allows the specification of the ```text``` to be displayed,
the ```font``` to use, the ```preferredSize```, and so forth. The values
for these name value pairs is fairly flexible, with strings, integers, booleans,
and dimensions all supported. You can also use the identifier for an already
declared component, which is often used with colors, images, and the like.

Spwing attempt to convert the values based on the type of the property. For 
example, you can specify the ```font``` as the string literal "Ariel 13" and
Spwing will create a font representing Ariel 13 on the fly. Spwing also 
defines a set of predefined identifiers, particularly for the colors. The standard
HTML colors are defined and can be used by referencing the color by name: 
```DarkSlateBlue```, ```LawnGreen```, etc. 

Similarly, a set of standard borders are defined:
- ```onePixelBlackLineBorder = LineBorder(Color.black, 1)```
- ```twoPixelBlackLineBorder = LineBorder(Color.black, 2)```
- ```threePixelBlackLineBorder = LineBorder(Color.black, 3)```
- ```fourPixelBlackLineBorder = LineBorder(Color.black, 4)```
- ```onePixelWhiteLineBorder = LineBorder(Color.white, 1)```
- ```twoPixelWhiteLineBorder = LineBorder(Color.white, 2)```
- ```threePixelWhiteLineBorder = LineBorder(Color.white, 3)```
- ```fourPixelWhiteLineBorder = LineBorder(Color.white, 4)```
- ```onePixelEmptyBorder = EmptyBorder(1, 1, 1, 1)```
- ```fivePixelEmptyBorder = EmptyBorder(5, 5, 5, 5)```
- ```tenPixelEmptyBorder = EmptyBorder(10, 10, 10, 10)```

Again, these can be referenced by name.

In addition to the Java Swing properties for the component, you can 
define a "value" for the component. This value is a String literal
that can be used when binding the component (for example, when
binding JRadioButtons to model enum properties). See the binding
section for more information.

Roadmap: In the future, there are plans to support the pseudo
properties "style" and "stylesheet" as well.

## Defaults
It can be onerous to define the same value for multiple components.
For example, it is very common for an application to want to define
a single font for all labels. Spwing simplifies this by allowing a
```defaults``` clause that definest the default value for each class
of control:

```agsl
defaults {
    JLabel( foreground=GreenAlpha, font="Serif-18"  );
}

```

The defaults clause looks like a normal component definition except
for the omission of the name. The defaults on a class will apply to
that class and all subclasses.

## Colors
Since it is common in user interfaces to use specific colors, SVWF
allows for the definition of colors:

```agsl
colors {
    DeepRed( 255, 0, 0 );
    DeepBlue( 0, 0, 255, 0 );
    GreenAlpha( 0.0, 1.0, 0.0, 0.75 );
    DeepGrey: 0x808080;
}
```

Once defined, colors are referenced by name.

Colors may be defined by RGB integers, RGBA integers, RGB floats,
RGBA floats, or a hex value as shown above.

## Image
Similarly to colors, SVWF allows for the definition of images.

```agsl
images {
    SpwingLogoURL: url "https://imageHost.com/Spwing.png";
    SpwingLogoResource: "Spwing.png" (model);
}
```

The first for defines the image as coming from a URL, which must be 
accessible at runtime.

The second form references the image by name as a resource based off
a class. The class may be specified as the model, controller,
application, or any uniquely named Spring bean. If omitted the class
will default to the model.

Note that the resource name is retrieved using platform-aware resource
semantics. See the README_PLATFORM_RESOURCES.md for more information.



## Bindings
SVWF allows you to connect view components to model properties through
bindings. Bindings can be made to any component property, although the 
property associated with the contents of the component are the most
common binding and are the only bindings that allow for bi-directional 
binding.

In bi-directional binding, changes to the contents of the component are
reflected in the model property. Bi-directional bindings require the model
to follow standard Java Bean syntax. Bi-directional bindings automatically
detect and block update loops.

Bindings can occur to a single control or a group of controls. In group
bindings, the value of the control will be considered as a part of the bind.
Group bindings are most commonly used for RadioButtons and the like. If 
all the components in the group are AbstractButton subclasses, a ButtonGroup
will automatically be created behind the scenes. This allows for intuitive 
mapping of complex UIs to model elements.

Group bindings may be given a name by specifying the name before the opening
brace of the group, separated by a color (":"). If specified, the group can be used as a shorthand 
during layout to refer to all the elements of the group. See the individual 
layout description for how these groups are handled.

Bindings may be to a model property directly, a SPEL expression, or a Flexpression.
Only direct model property bindings can be bidirectional.

In addition to the binding expression, one or more events can be defined for the 
view to listen to. If these events are supplied, the value of the expression will
be re-evaluated when the event occurs. In applications where the model property
cannot change except through the view, these events can be omitted. In more complex
application where the model properties might have non-obvious or externally generated
state changes, the events allow the view to stay in synch with the model.

An example is below. Note that in this example, if the person's name matches an icon
that icon will be displayed; if not the "missing" icon will be displayed instead. The
second text field is bound to the person's name, in lower case, with the state of the 
onOff flag appended. ```thing1``` and ```thing2``` define a radio button group that
changes the model "thing" property between enum values.

```agsl
bind {
    imageField.icon => "person.name" ["evtNameChange" ];
    textField1.text => "person.name" ["evtNameChange"];
    textField2.text => "person.name.toLowerCase() + ':' + isOnOff()" ["evtNameChange", "evtOnOffChange"];
    checkBox.selected => "onOff" ["evtOnOffChange"];
    g1: { thing1, thing2 }.selected => "thing" ["evtThingChanged"];
}

```

Model object:
```Java
public class MyModel extends BaseModel {

    public enum Things {Thing1, Thing2}

    @AllArgsConstructor
    public class Person implements Serializable {

        @Getter
        private String name;

        public void setName(String name) {
            this.name = name;
            MyModel.this.stateChanged("evtNameChange");
        }
    }

    @Getter
    private Things thing = Things.Thing1;

    @Getter
    private boolean onOff = true;

    @Getter
    private Person person = new Person("Bob");

    @SuppressWarnings("unused")
    public void setOnOff(boolean onOff) {
        this.onOff = onOff;
        this.stateChanged("evtOnOffChange");
    }

    public void setThing(Things thing) {
        this.thing = thing;
        this.stateChanged("evtThingChanged");
    }
}
```

Note that this model object uses Lombok annotations for 
conciseness and document events to trigger updates to the 
view when model elements are changed. The ```stateChanged```
method is inherited from the Spwing ```BaseModel``` convenience
class.



## Layout
Before components can be presented to the user, they have to be arranged
visually. Layouts allow this to happen, arranging components into a 
hierarchy that ultimately roots in a JFrame representing the window the user 
sees.

Layouts are described in the layout clause:

```agsl
layout {
    panel: boxLayout( imageField, textField1, textField2, thing1, thing2 );
    mainWindow: borderLayout( north=panel, south=button, east=checkBox);
}
```
Each layout statement starts by identifying a component, which must represent
a subclass of JContainer. This component becomes the container for the 
other components in the statement, based on the specified layout.

The type of layout is selected by name:
- flowLayout - FlowLayout
- boxLayout - BoxLayout
- borderLayout - BorderLayout

### Flow Layouts
Flow layouts take a list of components, which are added in the order specified.

```agsl
    panel: flowLayout( imageField, textField1, textField2, thing1, thing2 );
```

For flow layouts, adding a group is the same as adding all the elements of the 
group in the order specified.


### Box Layouts
Box layouts are similar to flow layouts, but allow for pseudo elements that 
create spaces or glue elements together. Box layouts may be horizontal (default)
or vertical in alignment. The alignment is specified after the keyword "boxLayout"
and before the elements of the layout are specified, and may be omitted.
- rigidArea( width, height ): A rigid area of the specified width and height
- horizontalGlue: Glue in the horizontal direction
- verticalGlue: Glue in the vertical direction
- A filler, which is specified by a list of sizes enclosed in parenthesis. All
specifications are optional, but at least one must be defined.
    - minSize(width, height): The filler will have the given minimum size
    - maxSize(width, height): The filler will have the given maximum size
    - prefSize(width, height): The filler has the given preferred size

```agsl
    panel: boxLayout horizontal( (minSize(10, 10)), imageField, rigidArea(20, 10), textField1, textField2, thing1, horizontalGlue, thing2 );
```

For box layouts, adding a group is the same as adding all the elements of the
group in the order specified.

### Border Layouts
Border layouts have five areas, defined as ```north```, ```south```, ```east```, ```west``` 
and ```center```. Each of these areas may contain a single component. 

```agsl
    mainWindow: borderLayout( north=panel, south=button, east=checkBox);
```

Since each of the five areas has to hold a single container, using a group
for any of these areas will create an anonymous JPanel containing all the 
elements in the group, added in order. The layout for this panel will be
a flow layout.


SWVF currently supports box, border and flow layouts. Other layouts are coming soon.



## Using SVWF files
The SVWF file can be used by associating the ```SVWFResourceViewFactory```
with a model as the view factory:

```agsl
@Model( viewFactory=SVWFResourceViewFactory.class)
public class MyModel {
...
}
```

This is the default view factory and can be omitted.

If the ```SVWFResourceViewFactory``` is used, it will search for a resource file
containing the svwf declarations in the following search order:

- Search for a resource with the same name as the simple name of the model class
in the resource directory for the model class
- If the model class simple name ends with "Model", search for a resource with the model
class simple name but replacing the trailing "Model" with "View" in the resource directory
for the model 
- Search for a resource with the same name as the simple name of the controller class 
in the resource directory for the controller class
- If the controller class simple name ends with "Controller", search for a resource with the
controller class simple name but replacing the trailing "Controller" with "View" in the
resource directory for the controller class.

In all cases, the resource is search using platform sensitivity (see README_PLATFORM_RESOURCES.md)
and will have the extension ".svwf"

If you want to specify the location of the SVWF file in another way, you can create your
own view factory from the ```SVWFViewFactory``` base class.


## Getting view objects in the controller or model
If you have the need for direct access to the view object in the model or controller,
simple create an instance field with the same type and name as the element. The 
element will be automatically added to the model/control instance after it is created.

```Java
    @Setter
    private JButton button;     // Will be defined from button:JButton(...) component
```


## Notes
SVWF is currently a single-pass interpreter, meaning the components
have to be defined before they are used in layouts or bindings.
The statements can be repeated as necessary.