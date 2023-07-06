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

