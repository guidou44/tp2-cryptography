package cipher;

import common.MathUtils;
import exceptions.InvalidParameterException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Shamir {

    private final SecureRandom random = new SecureRandom();

    public Shamir() {
    }

    /**
     * Fonction qui séparer le secret en n points pour l'algorithme de partage de secret de shamir.
     * Le paramètre 'primaryNumber' sert à générer les coefficients a(1) .. a(k-1).
     * Le paramètre 'secret' est le secret.
     * Le paramètre 'k' est le nombre de point minimum pour retrouver le secret
     * Le paramètre 'n' est le nombre total de points
     * */
    public List<ShamirPoint> splitSecret(int secret, int k, int n, int primaryNumber) {
        // préconditions sur les arguments
        if (secret >= primaryNumber || n >= primaryNumber || k > n || k <= 0) {
            throw new InvalidParameterException("Provided arguments do not respect shamir secret sharing algorithm :" +
                    System.lineSeparator() + "0 < k <= n < P; S < P");
        }

        // génération des coefficients
        List<Integer> coefficients = new ArrayList<>(); // list des coefficients : a0, a1, .., ak-1
        coefficients.add(secret); //ajout du secret comme a0
        for (int i = 1; i < k; i ++) {
            coefficients.add(random.nextInt(primaryNumber) + 1); // génération d'un coefficient aléatoire
        }

        List<ShamirPoint> shared = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            int polyEvalOfi = MathUtils.polynomialEvaluationOf(i, coefficients); // on calcule f(i)
            shared.add(new ShamirPoint(i, MathUtils.customModulo(polyEvalOfi, primaryNumber))); //on ajoute le nouveau point à la liste des points
        }

        return shared; //tous les points
    }

    /**
     * Retrouver un secret à partir des points. Si le nombre de points minimum n'est pas fournie,
     * le secret retrouvé ne sera tout simplement pas le bon.
     * */
    public int joinSecret(List<ShamirPoint> points, int primaryNumber) {

        int sum = 0;
        for (int currentPointIndex = 0; currentPointIndex < points.size(); currentPointIndex ++) {

            int xi = points.get(currentPointIndex).getX();
            int yi = points.get(currentPointIndex).getY();
            int numeratorProduct = 1;
            int denominatorProduct = 1;

            for (int otherPointIndex = 0; otherPointIndex < points.size(); otherPointIndex++) {

                if (currentPointIndex != otherPointIndex) {
                    numeratorProduct *= -points.get(otherPointIndex).getX(); //on évalue immédiatement le numérateur à x = 0;
                    denominatorProduct *= (xi - points.get(otherPointIndex).getX()); //on évalue le dénominateur
                }
            }

            //on calcul l'inverse du dénominateur dans Zn
            int inverseOfDenominatorProduct = MathUtils.modInverseOfAInZn(denominatorProduct, primaryNumber);
            sum += (yi * inverseOfDenominatorProduct * numeratorProduct); //on l'ajoute à la somme
        }

        return MathUtils.customModulo(sum, primaryNumber); //le secret est la somme modulo primaryNumber
    }
}
