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
Using JFormattedText
</p>

Spwing support JFormattedText instances, including binding the format to model properties and changing
them at runtime.

```java
    label: JFormattedTextField( name="birthday", value => "person.birthday", format = @dateTimeFormat ); 
```

Formatters are generally defined as beans. For convenience, Spwing predefines a set of formatters:

    - dateFormat: DateFormat.getDateInstance()
    - timeFormat: DateFormat.getTimeInstance()
    - dateTimeFormat: DateFormat.getDateTimeInstance()
    - utcFormat: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX") with timeZone = UTC
    - numberFormatPercent: NumberFormat.getPercentInstance()
    - numberFormat: NumberFormat.getNumberInstance()
    - numberFormatCurrency: NumberFormat.getCurrencyInstance()
    - numberFormatCompact: NumberFormat.getCompactNumberInstance()

These formats should be associated with date like objects (Date, Calendar, Instant) and number-like objects
(int, float, BigDecimal) respectively.

You can also create your own formatter beans (generally in your application class):

```java
    @Bean
    @Scope("singleton")
    public NumberFormat threeDigitPercentage() {
        NumberFormat threeDigitPercent = NumberFormat.getPercentInstance();
        threeDigitPercent.setMaximumFractionDigits(3);
        threeDigitPercent.setMinimumFractionDigits(3);
        return NumberFormat.getCompactNumberInstance();
    }

    @Bean
    @Scope( "singleton" )
    public MaskFormatter zip5Formatter() {
        try {
            return new MaskFormatter("#####" );
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


```

The class will also accept a String as a format. In this case the String should be 
parseable for a MaskFormatter, and the value bound to should be a String.

If you bind to the format and change it at runtime (which you can), you need to make
sure the new format matches the value the ```value``` property is bound to.