/**
 * jVCard remote uses a simple plain text protocol to 
 * transfer/retrieve/delete information from/to the server.
 * 
 * <p>
 * 
 * The protocol is based around <b>lines</b> of text and <b>blocks</b> of 
 * <b>lines</b>. The client sends <b>commands</b> while the server only 
 * answers with text data.
 * 
 * <p>
 * 
 * Some definitions:
 * <ul>
 * 		<li>A <b>line</b> is the text between two new-line characters (\n).
 * 		<li>A <b>block</b> is all the <b>lines</b> between two end-of-block 
 * <b>lines</b>.
 * 		<li>An end-of-block <b>line</b> is a special <b>line</b> 
 * containing only a dot (.).
 * 		<li>A <b>command</b> is <b>block</b> whose first <b>line</b> 
 * contains the command in upper case text and an optional text
 * argument, optionally followed by a second <b>block</b> of "input" data
 * </ul>
 * 
 * <p>
 * 
 * Upon connection, the first thing that happen is that the server sends 
 * a VERSION <b>command</b> (a <b>block</b> whose first <b>line</b> is 
 * <b><tt>VERSION 1</tt></b>, possibly followed by some help text 
 * <b>lines</b>).
 * 
 * <p>
 * 
 * The client <b>MUST</b> answer with another VERSION <b>command</b>.
 * 
 * <p>
 * 
 * From that time on, the client is allowed to send <b>commands</b>
 * as described by {@link be.nikiroo.jvcard.remote.Command}.
 * If the client doesn't follow the rules in 
 * {@link be.nikiroo.jvcard.remote.Command}, the server will close
 * the connection.
 * 
 * @author niki
 */
package be.nikiroo.jvcard.remote;