/* Generated By:JJTree: Do not edit this line. ASTsubBlock.java */

package uchicago.src.sim.parameter.rpl;

import java.util.List;

public class ASTsubBlock extends SimpleNode {

  public String blockName;

  public ASTsubBlock(int id) {
    super(id);
  }

  public ASTsubBlock(RPLParser p, int id) {
    super(p, id);
  }

  public String getBlockName() {
    return blockName;
  }

  public void preProcess(RPLCompiler compiler) {
    blockName = (String) children[0].getInfo();
    children[1].preProcess(compiler);
  }

  public void compile(RPLCompiler compiler) {

    // a subblock should be a parameter already defined in the compiler
    if (!compiler.isParameter(blockName)) {
      String message = "parameter '" + blockName + "' is undefined";
      throw compiler.createCompilerException(message, this.beginLine);
    }

    RPLParameter blockP = compiler.getParameter(blockName);

    ASTsuite suite = (ASTsuite)children[1];
    List list = suite.getConstants();
    for (int i = 0, n = list.size(); i < n; i++) {
      ASTConstant c = (ASTConstant)list.get(i);
      c.compile(compiler);
      blockP.addChildConstant(c.getRPLConstant());
    }

    list = suite.getReferences();
    for (int i = 0, n = list.size(); i < n; i++) {
      String varName = (String)list.get(i);
      if (!compiler.isParameter(varName)) {
        String message = "parameter reference '" + varName + "' is undefined";
        throw compiler.createCompilerException(message, this.beginLine);
      }

      RPLParameter p = compiler.getParameter(varName);
      p.addToParent(blockP);
    }

    list = suite.getSubBlocks();
    for (int i = 0, n = list.size(); i < n; i++) {
      ASTsubBlock block = (ASTsubBlock)list.get(i);
      block.compile(compiler);
      RPLParameter p = compiler.getParameter(block.getBlockName());
      p.addToParent(blockP);
    }
  }
}
