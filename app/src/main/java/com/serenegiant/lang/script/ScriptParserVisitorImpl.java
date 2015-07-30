package com.serenegiant.lang.script;

public class ScriptParserVisitorImpl implements ScriptParserVisitor, ScriptParserTreeConstants {
	public Object defaultVisit(SimpleNode node, Object data) {
		node.childrenAccept(this, data);
		return data;
	}

	@Override
	public Object visit(SimpleNode node, Object data) {
		return defaultVisit(node, data);
	}

	@Override
	public Object visit(ASTParse node, Object data) {
		return defaultVisit(node, data);
	}

	@Override
	public Object visit(ASTConditional node, Object data) {
		return defaultVisit(node, data);
	}

	@Override
	public Object visit(ASTConstant node, Object data) {
		return defaultVisit(node, data);
	}

	@Override
	public Object visit(ASTLogicalOR node, Object data) {
		final boolean v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsBool(this, null);
		final boolean w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsBool(this, null);
		return v || w;
	}

	@Override
	public Object visit(ASTLogicalAND node, Object data) {
		final boolean v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsBool(this, null);
		final boolean w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsBool(this, null);
		return v && w;
	}

	@Override
	public Object visit(ASTInclusiveOR node, Object data) {
		final int v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsInt(this, null);
		final int w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsInt(this, null);
		return v | w;
	}

	@Override
	public Object visit(ASTExclusiveOR node, Object data) {
		final int v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsInt(this, null);
		final int w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsInt(this, null);
		return v ^ w;
	}

	@Override
	public Object visit(ASTAnd node, Object data) {
		final int v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsInt(this, null);
		final int w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsInt(this, null);
		return v & w;
	}

	@Override
	public Object visit(ASTEquality node, Object data) {
		final float v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsFloat(this, null);
		final float w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsFloat(this, null);
		return v == w;
	}

	@Override
	public Object visit(ASTNotEquality node, Object data) {
		final float v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsFloat(this, null);
		final float w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsFloat(this, null);
		return v != w;
	}

	@Override
	public Object visit(ASTRelationalLT node, Object data) {
		final float v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsFloat(this, null);
		final float w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsFloat(this, null);
		return v < w;
	}

	@Override
	public Object visit(ASTRelationalGT node, Object data) {
		final float v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsFloat(this, null);
		final float w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsFloat(this, null);
		return v > w;
	}

	@Override
	public Object visit(ASTRelationalLTE node, Object data) {
		final float v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsFloat(this, null);
		final float w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsFloat(this, null);
		return v <= w;
	}

	@Override
	public Object visit(ASTRelationalGTE node, Object data) {
		final float v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsFloat(this, null);
		final float w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsFloat(this, null);
		return v >= w;
	}

	@Override
	public Object visit(ASTShiftRight node, Object data) {
		final int v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsInt(this, null);
		final int w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsInt(this, null);
		return v >>> w;
	}

	@Override
	public Object visit(ASTShiftLeft node, Object data) {
		final int v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsInt(this, null);
		final int w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsInt(this, null);
		return v << w;
	}

	@Override
	public Object visit(ASTAdditivePlus node, Object data) {
		float result = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsFloat(this, null);
		result += ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsFloat(this, null);
		return result;
	}

	@Override
	public Object visit(ASTAdditiveMinus node, Object data) {
		float result = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsFloat(this, null);
		result -= ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsFloat(this, null);
		return result;
	}

	@Override
	public Object visit(ASTMultiplicative node, Object data) {
		float result = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsFloat(this, null);
		result *= ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsFloat(this, null);
		return result;
	}

	@Override
	public Object visit(ASTIdentifier node, Object data) {
		return defaultVisit(node, data);
	}

	@Override
	public Object visit(ASTIntegerConst node, Object data) {
		return node.jjtGetAsInt();
	}

	@Override
	public Object visit(ASTFloatConst node, Object data) {
		return node.jjtGetAsFloat();
	}

	@Override
	public Object visit(ASTCharConst node, Object data) {
		return node.jjtGetValue();
	}

	@Override
	public Object visit(ASTStringConst node, Object data) {
		return node.jjtGetValue();
	}
}
