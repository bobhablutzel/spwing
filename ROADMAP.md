## Roadmap

Some thoughts on what still needs to be done in Spwing, in no particular order.

- Hierarchical models. Models should contain sub-models (with associated
  sub-controllers and sub-views), including the ability to dynamically
  maintain collections of sub-models associated to (e.g.) tables. This is
  probably the thorniest problem still to solve, but will give a very
  clean programming model.
- Preference dialogs, with associated storage of preferences. Preferences
should be automatically stored with documents (e.g. window position and size)
as well as settings that the user can manipulate directly
- Revisit the document "dirty" mechanism; make it less invasive
- Splash screen
- Example program for creating stand-alone deployable applications
- General performance tuning - an on-going process
- Dynamic menus (for open windows, lists of fonts, etc.)
- Replace the current JSON based menu specification with a DSL
- Support for additional Swing components (NOTE - this list was
mechanically created and likely contains components that will not
be directly supported as they are internal to other components or abstract)
  - javax.swing.Box
  - javax.swing.CellRendererPane
  - javax.swing.JSpinner.DateEditor
  - javax.swing.JSpinner.DefaultEditor
  - javax.swing.DefaultListCellRenderer
  - javax.swing.JCheckBoxMenuItem
  - javax.swing.JColorChooser
  - javax.swing.JComboBox
  - javax.swing.JInternalFrame.JDesktopIcon
  - javax.swing.JDesktopPane
  - javax.swing.JEditorPane
  - javax.swing.JFileChooser
  - javax.swing.JFormattedTextField
  - javax.swing.JInternalFrame
  - javax.swing.JLayer
  - javax.swing.JLayeredPane
  - javax.swing.JList
  - javax.swing.JMenu
  - javax.swing.JMenuBar
  - javax.swing.JMenuItem
  - javax.swing.JOptionPane
  - javax.swing.JPasswordField
  - javax.swing.JPopupMenu
  - javax.swing.JProgressBar
  - javax.swing.JRadioButtonMenuItem
  - javax.swing.JRootPane
  - javax.swing.JScrollBar
  - javax.swing.JScrollPane
  - javax.swing.JSeparator
  - javax.swing.JSlider
  - javax.swing.JSpinner
  - javax.swing.JSplitPane
  - javax.swing.JTabbedPane
  - javax.swing.JTable
  - javax.swing.JTextArea
  - javax.swing.JTextPane
  - javax.swing.JToggleButton.
  - javax.swing.JToolBar
  - javax.swing.JToolTip
  - javax.swing.JTree
  - javax.swing.JViewport
  - javax.swing.JSpinner.ListEditor
  - javax.swing.JSpinner.NumberEditor
  - javax.swing.JPopupMenu.Separator
  - javax.swing.JToolBar.Separator
  - javax.swing.DefaultListCellRenderer.UIResource
- Support specifically for popup menus tied to lists, enums, etc.
- Property event bind operator ==> which derives the trigger event by convention rather than specification
  (this probably depends on the hierarchical model support)
- Ensure all classes are properly documented.
- Support for more layout managers
  - BorderLayout (done)
  - BoxLayout (done)
  - CardLayout (tbd)
  - FlowLayout (done)
  - GridBagLayout (done)
  - GridLayout (tbd)
  - GroupLayout (tbd)
  - SpringLayout (tbd)


# Goals
- 0.6 release   Support all layout managers and components
- 0.7 release   Support nested / hierarchical models
- 0.8 release   Revisit menu management / dynamic menu support
- 0.9 release   Additional features TBD plus a lot of debugging & performance tuning
- 1.0 release   Stable and fully functional

