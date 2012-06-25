package com.wikispaces.lsfn.Shared;

import org.junit.*;

public class TestTests {
	@Test public void ShouldPass() {
	}
	
	@Test(expected=Exception.class) public void ShouldThrow() throws Exception {
		throw new Exception("Test expects an exception.");
	}
}
