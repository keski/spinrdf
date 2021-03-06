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

package org.spinrdf.model.update.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.spinrdf.arq.ARQ2SPIN;
import org.spinrdf.model.*;
import org.spinrdf.model.impl.AbstractSPINResourceImpl;
import org.spinrdf.model.print.PrintContext;
import org.spinrdf.model.update.Update;
import org.spinrdf.util.JenaDatatypes;
import org.spinrdf.util.JenaUtil;
import org.spinrdf.vocabulary.SP;

public abstract class UpdateImpl extends AbstractSPINResourceImpl implements Update {

	public UpdateImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	public ElementList getWhere() {
		Statement whereS = getProperty(SP.where);
		if(whereS != null) {
			Element element = SPINFactory.asElement(whereS.getResource());
			return (ElementList) element;
		}
		else {
			return null;
		}
	}

	
	public boolean isSilent() {
		return hasProperty(SP.silent, JenaDatatypes.TRUE);
	}


	@Override
	public void print(PrintContext p) {
		String text = ARQ2SPIN.getTextOnly(this);
		if(text != null) {
			p.print(text);
		}
		else {
			printSPINRDF(p);
		}
	}
	
	
	protected abstract void printSPINRDF(PrintContext p);


	protected void printGraphDefaultNamedOrAll(PrintContext p) {
		Resource graph = JenaUtil.getResourceProperty(this, SP.graphIRI);
		if(graph != null) {
			p.printKeyword("GRAPH");
			p.print(" ");
			p.printURIResource(graph);
		}
		else if(hasProperty(SP.default_, JenaDatatypes.TRUE)) {
			p.printKeyword("DEFAULT");
		}
		else if(hasProperty(SP.named, JenaDatatypes.TRUE)) {
			p.printKeyword("NAMED");
		}
		else if(hasProperty(SP.all, JenaDatatypes.TRUE)) {
			p.printKeyword("ALL");
		}
	}


	protected void printGraphIRIs(PrintContext p, String keyword) {
		List<String> graphIRIs = new ArrayList<String>();
		{
			Iterator<Statement> it = listProperties(SP.graphIRI);
			while(it.hasNext()) {
				Statement s = it.next();
				if(s.getObject().isURIResource()) {
					graphIRIs.add(s.getResource().getURI());
				}
			}
			Collections.sort(graphIRIs);
		}
		for(String graphIRI : graphIRIs) {
			p.print(" ");
			if(keyword != null) {
				p.printKeyword(keyword);
				p.print(" ");
			}
			p.printURIResource(getModel().getResource(graphIRI));
		}
	}


	protected void printSilent(PrintContext p) {
		if(isSilent()) {
			p.printKeyword("SILENT");
			p.print(" ");
		}
	}
	
	
	protected boolean printTemplates(PrintContext p, Property predicate, String keyword, boolean force, Resource graphIRI) {
		List<RDFNode> nodes = getList(predicate);
		if(!nodes.isEmpty() || force) {
			if(keyword != null) {
				p.printIndentation(p.getIndentation());
				p.printKeyword(keyword);
			}
			p.print(" {");
			p.println();
			if(graphIRI != null) { // Legacy triple
				p.setIndentation(p.getIndentation() + 1);
				p.printIndentation(p.getIndentation());
				p.printKeyword("GRAPH");
				p.print(" ");
				printVarOrResource(p, graphIRI);
				p.print(" {");
				p.println();
			}
			for(RDFNode node : nodes) {
				p.printIndentation(p.getIndentation() + 1);
				if(node.canAs(NamedGraph.class)) {
					NamedGraph namedGraph = node.as(NamedGraph.class);
					p.setIndentation(p.getIndentation() + 1);
					p.setNamedBNodeMode(true);
					namedGraph.print(p);
					p.setNamedBNodeMode(false);
					p.setIndentation(p.getIndentation() - 1);
				}
				else {
					TripleTemplate template = node.as(TripleTemplate.class);
					template.print(p);
				}
				p.print(" .");
				p.println();
			}
			if(graphIRI != null) {
				p.printIndentation(p.getIndentation());
				p.setIndentation(p.getIndentation() - 1);
				p.print("}");
				p.println();
			}
			p.printIndentation(p.getIndentation());
			p.print("}");
			return true;
		}
		else {
			return false;
		}
	}


	protected void printWhere(PrintContext p) {
		p.printIndentation(p.getIndentation());
		p.printKeyword("WHERE");
		printNestedElementList(p, SP.where);
	}
}
