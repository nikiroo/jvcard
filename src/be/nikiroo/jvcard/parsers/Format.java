package be.nikiroo.jvcard.parsers;

/**
 * The parsing format for the contact data.
 * 
 * @author niki
 * 
 */
public enum Format {
	/**
	 * vCard 2.1 file format. Will actually accept any version as input.
	 */
	VCard21,
	/**
	 * (Al)Pine Contact Book format, also called abook (usually .addressbook).
	 */
	Abook
}
