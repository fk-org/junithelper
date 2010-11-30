package org.junithelper.core.config;

import static org.junit.Assert.*;
import org.junit.Test;

public class MessageValueTest {

	@Test
	public void type() throws Exception {
		assertNotNull(MessageValue.class);
	}

	@Test
	public void instantiation() throws Exception {
		MessageValue target = new MessageValue();
		assertNotNull(target);
	}

	@Test
	public void initialize_A$String() throws Exception {
		MessageValue target = new MessageValue();
		// given
		String lang = null;
		// when
		target.initialize(lang);
		// then
		assertNotNull(target.getAutoGeneratedTODOMessage());
	}

	@Test
	public void get_A$String() throws Exception {
		MessageValue target = new MessageValue();
		// given
		target.initialize(null);
		String key = "Message.exempliGratia";
		// when
		String actual = target.get(key);
		// then
		String expected = "e.g.";
		assertEquals(expected, actual);
	}

	@Test
	public void getAutoGeneratedTODOMessage_A$() throws Exception {
		MessageValue target = new MessageValue();
		// given
		target.initialize(null);
		// when
		String actual = target.getAutoGeneratedTODOMessage();
		// then
		// e.g. : verify(mocked).called();
		String expected = "TODO auto-generated by JUnit Helper.";
		assertEquals(expected, actual);
	}

	@Test
	public void getExpectedExceptionNotThrownMessage_A$() throws Exception {
		MessageValue target = new MessageValue();
		// given
		target.initialize(null);
		// when
		String actual = target.getExpectedExceptionNotThrownMessage();
		// then
		// e.g. : verify(mocked).called();
		String expected = "Expected exception was not thrown!";
		assertEquals(expected, actual);
	}

	@Test
	public void getExempliGratia_A$() throws Exception {
		MessageValue target = new MessageValue();
		// given
		target.initialize(null);
		// when
		String actual = target.getExempliGratia();
		// then
		// e.g. : verify(mocked).called();
		String expected = "e.g.";
		assertEquals(expected, actual);
	}

}