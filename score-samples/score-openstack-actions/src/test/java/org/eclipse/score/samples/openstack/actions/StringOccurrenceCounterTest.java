/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.samples.openstack.actions;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Date: 8/20/2014
 *
 * @author lesant
 */
public class StringOccurrenceCounterTest {
	private static final long DEFAULT_TIMEOUT = 5000;

	@Test(timeout = DEFAULT_TIMEOUT)
	public void testExecuteWithMissingInputs(){

		StringOccurrenceCounter stringOccurrenceCounter = new StringOccurrenceCounter();
		Map<String, String> returnResult = stringOccurrenceCounter.execute(null, "string", "true");
		assertEquals("Return code not as expected", "1", returnResult.get("returnCode"));

		returnResult = stringOccurrenceCounter.execute("string1, string2", null, "true");
		assertEquals("Return code not as expected", "1", returnResult.get("returnCode"));

		returnResult = stringOccurrenceCounter.execute("string1, string2", "string1", null);
		assertEquals("Return code not as expected", "1", returnResult.get("returnCode"));

	}
	@Test(timeout = DEFAULT_TIMEOUT)
	public void testExecute(){

		StringOccurrenceCounter stringOccurrenceCounter = new StringOccurrenceCounter();
		Map<String, String> returnResult = stringOccurrenceCounter.execute("string1, string2", "string1", "false");
		assertEquals("Return code not as expected", "0", returnResult.get("returnCode"));
		assertEquals("Result not as expected", "1", returnResult.get("returnResult"));

		returnResult = stringOccurrenceCounter.execute("string2, string2", "string2", "false");
		assertEquals("Result not as expected", "2", returnResult.get("returnResult"));

		returnResult = stringOccurrenceCounter.execute("string2, string2", "String2", "true");
		assertEquals("Result not as expected", "2", returnResult.get("returnResult"));
		returnResult = stringOccurrenceCounter.execute("string2, string2", "String2", "false");
		assertEquals("Result not as expected", "0", returnResult.get("returnResult"));

	}
}