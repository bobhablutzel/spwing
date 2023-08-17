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
Using ComboBoxes
</p>

For JComboBoxes you can set or bind to the "items" and "selected" properties, and generally have to bind to 
both of them. However, if the "selected" property is bound to an enum value, the "items" will automatically
be set to the valid values for the enum. You can of course override this (e.g. setting to a subset of the 
enum values) by specifying the items directly.
