package fr.cs.orekit.errors;

/** This class is the base class for all specific exceptions thrown by
 * during the propagation computation.
 *
 * @author  Luc Maisonobe
 */
public class PropagationException extends OrekitException {

    /** Serializable UID. */
    private static final long serialVersionUID = 3644973045303340556L;

    /** Simple constructor.
     * Build an exception with a translated and formatted message
     * @param specifier format specifier (to be translated)
     * @param parts parts to insert in the format (no translation)
     */
    public PropagationException(final String specifier, final Object[] parts) {
        super(specifier, parts);
    }

    /** Simple constructor.
     * Build an exception from a cause and with a specified message
     * @param message descriptive message
     * @param cause underlying cause
     */
    public PropagationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
