/**
 * Barrister RPC Java bindings.  Also includes a code generator for creating
 * client and server stubs based on a IDL JSON file.
 * <p>
 * Classes of note:
 * <ul>
 *   <li>{@link com.bitmechanic.barrister.Server} - Used when writing servers. Register your
 *       interface implementations with it. Dispatches requests to handlers.</li>
 *   <li>{@link com.bitmechanic.barrister.Idl2Java} - Generates client/server stubs based on a Barrister
 *       IDL JSON file</li>
 * </ul>
 *
 * @see <a href="https://github.com/coopernurse/barrister-java">barrister-java GitHub repository</a>
 * @see <a href="http://barrister.bitmechanic.com/">main Barrister web site</a>
 */
package com.bitmechanic.barrister;