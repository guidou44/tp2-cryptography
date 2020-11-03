package common;

/**
* Cette enum sert à storer les opérations ou actions possibles dans des constantes
* */
public enum Action {
  ENCRYPT("enc"),
  DECRYPT("dec");

  private String paramName;

  Action(String paramName) {
    this.paramName = paramName;
  }


  /**
   * retourne le paramètre console attendue pour l'opération ou action
   * */
  public String GetParameterName() {
    return this.paramName;
  }
}
