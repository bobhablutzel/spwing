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
Using Spinners
</p>

Spwing supports three basic JSpinner types: JNumberSpinner, JDateSpinner, and JListSpinner. These
act as JSpinner instances with Number, Date, and List models respectively. The framework handles 
the model transparently, so you can directly reference model fields (such as min and max for the
Number models) when defining the spinner:

```
            JNumberSpinner( value => "model.counter", min = 1, max = 100 );
            JDateSpinner( value => "model.birthday", start = "01/01/2023" );
```

Note that dates will automatically be converted from strings.

