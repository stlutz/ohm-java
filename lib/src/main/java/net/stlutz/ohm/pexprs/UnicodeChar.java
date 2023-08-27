package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.TerminalNode;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UnicodeChar extends Prim {
    public String category;
    public Pattern pattern;
    
    public UnicodeChar(String category) {
        super();
        this.category = category;
        pattern = unicodeCategoryPatterns.get(category);
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        int matchLength = inputStream.match(pattern);
        if (matchLength >= 0) {
            inputStream.advance(matchLength);
            evalContext.pushBinding(TerminalNode.get(matchLength), originalPosition);
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public void toString(StringBuilder sb) {
        sb.append("\\p{");
        sb.append(category);
        sb.append('}');
    }
    
    @Override
    public void toDisplayString(StringBuilder sb) {
        sb.append("Unicode [");
        sb.append(category);
        sb.append("] character");
    }
    
    public static final Map<String, Pattern> unicodeCategoryPatterns;
    
    static {
        unicodeCategoryPatterns = new HashMap<String, Pattern>(50);
        
        String[] realCategories = new String[]{"Cc", // Other, Control
            "Cf", // Other, Format
            "Cn", // Other, Not Assigned (no characters in the file have this property)
            "Co", // Other, Private Use
            "Cs", // Other, Surrogate
            "LC", // Letter, Cased
            "Ll", // Letter, Lowercase
            "Lm", // Letter, Modifier
            "Lo", // Letter, Other
            "Lt", // Letter, Titlecase
            "Lu", // Letter, Uppercase
            "Mc", // Mark, Spacing Combining
            "Me", // Mark, Enclosing
            "Mn", // Mark, Nonspacing
            "Nd", // Number, Decimal Digit
            "Nl", // Number, Letter
            "No", // Number, Other
            "Pc", // Punctuation, Connector
            "Pd", // Punctuation, Dash
            "Pe", // Punctuation, Close
            "Pf", // Punctuation, Final quote (may behave like Ps or Pe depending on usage)
            "Pi", // Punctuation, Initial quote (may behave like Ps or Pe depending on usage)
            "Po", // Punctuation, Other
            "Ps", // Punctuation, Open
            "Sc", // Symbol, Currency
            "Sk", // Symbol, Modifier
            "Sm", // Symbol, Math
            "So", // Symbol, Other
            "Zl", // Separator, Line
            "Zp", // Separator, Paragraph
            "Zs", // Separator, Space
        };
        
        for (String realCategory : realCategories) {
            unicodeCategoryPatterns.put(realCategory, Pattern.compile("\\p{%s}".formatted(realCategory)));
        }
        
        // These two are not real Unicode categories, but are useful for Ohm.
        // L is a combination of all the letter categories.
        unicodeCategoryPatterns.put("L", Pattern.compile("\\p{IsLetter}"));
        // Ltmo is a combination of Lt, Lm and Lo.
        unicodeCategoryPatterns.put("Ltmo", Pattern.compile("\\p{Lt}|\\p{Lm}|\\p{Lo}"));
    }
}
