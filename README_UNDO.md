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
Undo processing
</p>

Spwing provides a powerful undo/redo capability provided out of the box. Using this capability not only 
allows the user to undo/redo actions, but also ties those use actions seamlessly to the document file state
so that Spwing knows when documents need to be saved before closing.

The core of the functionality is provided by the Swing class ```UndoManager```. Behind the scenes, Spwing
subclasses this to provide additional functionality, but the general interface is the same. You can
add any ```UndoableEdit``` subclass to the UndoManager. However, there are advantages to using the 
Spwing subclasses (discussed below).

### Command aware undo processing

The challenge with undo processing is that both the model and the UI will generate undoable edits. Take the
example of a JTextField that is bound to a model property. Changes to that field can occur:
- When the user types in that field
- Through cut/copy actions
- Through model logic, such as loading from a database

You want all of these to be undoable, but in a way that makes sense to the end user: if they made the 
change to the JTextField through a menu command ("Load Data"), then the undo stack should have the menu command
(not the JTextField.document UndoableEdits created when JTextField.setText() is call) at the top of the stack.

In order to simplify this, the UndoManager provided by Spwing is aware of the ```ChangeCommand``` class. This 
class blocks any AWT/Swing generated UndoableEdits that arise during the execution of the ```ChangeCommand```. 
This leaves the ```ChangeCommand``` instance at the top of the undo stack. The framework handles undo and redo
similarly. ```ChangeCommand``` instances also let you specify the command name, which is localized via
property files and added to the label for the Undo/Redo menu items.

In most cases, you want your model modifying actions to occur through a ```ChangeCommand``` subclass. If the 
change is a simple model property change (via a setter), the ```PropertyChangeCommand``` provides a simplified
interface where you just specify the new value, the getter, the setter, and the command name.

### Tracking changes

The Spwing undo manager also is integrated with the default file saving activities. The undo manager knows the
last time the document was saved, and whether commands have be executed since then. The undo manager only
count commands that have remained executed since the last save; if you perform and undo a command the file is
considered to be the same as the last time it was saved.

### Menu processing
The default Spwing menu has command cmdUndo and cmdRedo as part of the Edit menu. The built-in Spwing
processing handles these commands automatically, including changing the text of the commands to match
the name of the last command executed.

### Accessing the UndoManager.

There are three ways to access the undo manager, from easy to complex:

- The most common way is to add a ```UndoManager``` parameter to the handler or listener methods. The
framework will recognize these parameter and automatically pass in the active undo manager instance.

```java
    public void onButton_Clicked(final UndoManager undoManager) { ... }

```

- Alternatively, you can get the undo manager from the ```ApplicationContext``` instance. You should
only do this for routines that have to get to the undo manager but aren't called via the handler, enabler,
or listener support (i.e. aren't called by an Invoker):
```java
        Spwing spwing = applicationContext.getBean(Spwing.class);
        UndoManager undoManager = spwing.getDocumentScopeManager().getActiveSession().getUndoManager();
```
