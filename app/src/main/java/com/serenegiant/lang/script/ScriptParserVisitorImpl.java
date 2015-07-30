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
	public Object visit(ASTCompoundStatement node, Object data) {
		// FIXME 未実装
		return defaultVisit(node, data);
	}

	@Override
	public Object visit(ASTStatementIf node, Object data) {
		final boolean v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsBool(this, null);
		if (v) {
			node.jjtGetChild(1).jjtAccept(this, data);
		} else if (node.jjtGetNumChildren() > 1) {
			node.jjtGetChild(2).jjtAccept(this, data);
		}
		return v;
	}

	@Override
	public Object visit(ASTStatementSwitch node, Object data) {
		// FIXME 未実装
		return null;
	}

	@Override
	public Object visit(ASTStatementWhile node, Object data) {
		boolean v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsBool(this, null);
		if (node.jjtGetNumChildren() > 1) {
			while (v) {
				node.jjtGetChild(1).jjtAccept(this, null);
				v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsBool(this, null);
			}
		}
		return v;
	}

	@Override
	public Object visit(ASTStatementDoWhile node, Object data) {
		boolean v = true;
		do {
			node.jjtGetChild(0).jjtAccept(this, data);
			v = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsBool(this, null);
		} while (v);
		return v;
	}

	public Object visit(ASTStatementFor node, Object data) {
		node.jjtGetChild(0).jjtAccept(this, data);
		boolean v = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsBool(this, null);
		while (v) {
			node.jjtGetChild(3).jjtAccept(this, data);
			v = ((SimpleNode)node.jjtGetChild(2)).jjtAcceptAsBool(this, null);
		}
		return v;
	}

	@Override
	public Object visit(ASTForInitStatement node, Object data) {
		final int n = node.jjtGetNumChildren();
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				node.jjtGetChild(i).jjtAccept(this, null);
			}
		}
		return n;
	}

	@Override
	public Object visit(ASTExpressionStatement node, Object data) {
		// FIXME 未実装
		return defaultVisit(node, data);
	}

	@Override
	public Object visit(ASTForUpdateStatement node, Object data) {
		final int n = node.jjtGetNumChildren();
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				node.jjtGetChild(i).jjtAccept(this, null);
			}
		}
		return n;
	}

	@Override
	public Object visit(ASTStatementGoto node, Object data) {
		// FIXME 未実装
		return defaultVisit(node, data);
	}

	@Override
	public Object visit(ASTStatementContinue node, Object data) {
		// FIXME 未実装
		return defaultVisit(node, data);
	}

	@Override
	public Object visit(ASTStatementBreak node, Object data) {
		// FIXME 未実装
		return defaultVisit(node, data);
	}

	@Override
	public Object visit(ASTStatementReturn node, Object data) {
		// FIXME 未実装
		return defaultVisit(node, data);
	}

	@Override
	public Object visit(ASTConditional node, Object data) {
		// FIXME 未実装
		return defaultVisit(node, data);
	}

	@Override
	public Object visit(ASTConstant node, Object data) {
		// FIXME 未実装
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
		final int v = ((SimpleNode) node.jjtGetChild(0)).jjtAcceptAsInt(this, null);
		final int w = ((SimpleNode) node.jjtGetChild(1)).jjtAcceptAsInt(this, null);
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
		final int w = ((SimpleNode) node.jjtGetChild(1)).jjtAcceptAsInt(this, null);
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
	public Object visit(ASTMultiplicativeMult node, Object data) {
		final float v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsFloat(this, null);
		final float w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsFloat(this, null);
		return v * w;
	}

	@Override
	public Object visit(ASTMultiplicativeDiv node, Object data) {
		final float v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsFloat(this, null);
		final float w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsFloat(this, null);
		return v / w;
	}

	@Override
	public Object visit(ASTMultiplicativeMod node, Object data) {
		final float v = ((SimpleNode)node.jjtGetChild(0)).jjtAcceptAsFloat(this, null);
		final float w = ((SimpleNode)node.jjtGetChild(1)).jjtAcceptAsFloat(this, null);
		return v % w;
	}

	@Override
	public Object visit(ASTIdentifier node, Object data) {
		// FIXME 未実装
		return defaultVisit(node, data);
	}
}
