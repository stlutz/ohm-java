package net.stlutz.ohm.pexprs;

import net.stlutz.ohm.InputStream;
import net.stlutz.ohm.OhmException;
import net.stlutz.ohm.TerminalNode;

import java.util.ArrayList;
import java.util.Collection;

public class UnicodeChar extends Prim {
    private final int categories;
    
    public UnicodeChar(byte[] categories) {
        int bitset = 0;
        for (byte category : categories) {
            if (category > 31) {
                throw new OhmException("Implementation cannot handle the given Unicode category");
            }
            bitset |= 1 << category;
        }
        this.categories = bitset;
    }
    
    @Override
    public boolean eval(EvalContext evalContext, InputStream inputStream, int originalPosition) {
        if (!inputStream.atEnd()) {
            int nextCodePointCategory = Character.getType(inputStream.nextCodePoint());
            if (((categories >> nextCodePointCategory) & 1) != 0) {
                int matchLength = inputStream.offsetTo(originalPosition);
                evalContext.pushBinding(TerminalNode.get(matchLength), originalPosition);
                return true;
            }
        }
        return false;
    }
    
    private Collection<String> getShorthands() {
        Collection<String> shorthands = new ArrayList<>();
        for (int i = 0; i < unicodeCategoryShorthands.length; i++) {
            if (((categories >> i) & 1) != 0) {
                shorthands.add(unicodeCategoryShorthands[i]);
            }
        }
        return shorthands;
    }
    
    @Override
    public void toString(StringBuilder sb) {
        sb.append("\\p{");
        sb.append(String.join("|", getShorthands()));
        sb.append('}');
    }
    
    private static final String[] unicodeCategoryShorthands;
    
    static {
        unicodeCategoryShorthands = new String[31];
        unicodeCategoryShorthands[Character.CONTROL] = "Cc"; // Other, Control
        unicodeCategoryShorthands[Character.FORMAT] = "Cf"; // Other, Format
        unicodeCategoryShorthands[Character.UNASSIGNED] = "Cn"; // Other, Not Assigned (no characters in the file have this property)
        unicodeCategoryShorthands[Character.PRIVATE_USE] = "Co"; // Other, Private Use
        unicodeCategoryShorthands[Character.SURROGATE] = "Cs"; // Other, Surrogate
        unicodeCategoryShorthands[Character.LOWERCASE_LETTER] = "Ll"; // Letter, Lowercase
        unicodeCategoryShorthands[Character.MODIFIER_LETTER] = "Lm"; // Letter, Modifier
        unicodeCategoryShorthands[Character.OTHER_LETTER] = "Lo"; // Letter, Other
        unicodeCategoryShorthands[Character.TITLECASE_LETTER] = "Lt"; // Letter, Titlecase
        unicodeCategoryShorthands[Character.UPPERCASE_LETTER] = "Lu"; // Letter, Uppercase
        unicodeCategoryShorthands[Character.COMBINING_SPACING_MARK] = "Mc"; // Mark, Spacing Combining
        unicodeCategoryShorthands[Character.ENCLOSING_MARK] = "Me"; // Mark, Enclosing
        unicodeCategoryShorthands[Character.NON_SPACING_MARK] = "Mn"; // Mark, Nonspacing
        unicodeCategoryShorthands[Character.DECIMAL_DIGIT_NUMBER] = "Nd"; // Number, Decimal Digit
        unicodeCategoryShorthands[Character.LETTER_NUMBER] = "Nl"; // Number, Letter
        unicodeCategoryShorthands[Character.OTHER_NUMBER] = "No"; // Number, Other
        unicodeCategoryShorthands[Character.CONNECTOR_PUNCTUATION] = "Pc"; // Punctuation, Connector
        unicodeCategoryShorthands[Character.DASH_PUNCTUATION] = "Pd"; // Punctuation, Dash
        unicodeCategoryShorthands[Character.END_PUNCTUATION] = "Pe"; // Punctuation, Close
        unicodeCategoryShorthands[Character.FINAL_QUOTE_PUNCTUATION] = "Pf"; // Punctuation, Final quote (may behave like Ps or Pe depending on usage)
        unicodeCategoryShorthands[Character.INITIAL_QUOTE_PUNCTUATION] = "Pi"; // Punctuation, Initial quote (may behave like Ps or Pe depending on usage)
        unicodeCategoryShorthands[Character.OTHER_PUNCTUATION] = "Po"; // Punctuation, Other
        unicodeCategoryShorthands[Character.START_PUNCTUATION] = "Ps"; // Punctuation, Open
        unicodeCategoryShorthands[Character.CURRENCY_SYMBOL] = "Sc"; // Symbol, Currency
        unicodeCategoryShorthands[Character.MODIFIER_SYMBOL] = "Sk"; // Symbol, Modifier
        unicodeCategoryShorthands[Character.MATH_SYMBOL] = "Sm"; // Symbol, Math
        unicodeCategoryShorthands[Character.OTHER_SYMBOL] = "So"; // Symbol, Other
        unicodeCategoryShorthands[Character.LINE_SEPARATOR] = "Zl"; // Separator, Line
        unicodeCategoryShorthands[Character.PARAGRAPH_SEPARATOR] = "Zp"; // Separator, Paragraph
        unicodeCategoryShorthands[Character.SPACE_SEPARATOR] = "Zs"; // Separator, Space
    }
}
