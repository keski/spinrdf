/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.spinrdf.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.spinrdf.model.Argument;
import org.spinrdf.vocabulary.SP;
import org.spinrdf.vocabulary.SPL;


public class ArgumentImpl extends AbstractAttributeImpl implements Argument {
	
	public ArgumentImpl(Node node, EnhGraph eg) {
		super(node, eg);
	}
	
	
	public Integer getArgIndex() {
		String varName = getVarName();
		if(varName != null) {
			return SP.getArgPropertyIndex(varName);
		}
		else {
			return null;
		}
	}


	public RDFNode getDefaultValue() {
		Statement s = getProperty(SPL.defaultValue);
		if(s != null) {
			return s.getObject();
		}
		else {
			return null;
		}
	}


	public String getVarName() {
		Property argProperty = getPredicate();
		if(argProperty != null) {
			return argProperty.getLocalName();
		}
		else {
			return null;
		}
	}


	public boolean isOptional() {
		Statement s = getProperty(SPL.optional);
		if(s != null && s.getObject().isLiteral()) {
			return s.getBoolean();
		}
		else {
			return false;
		}
	}
}
