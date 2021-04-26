module.exports = {
  extends: ["react-app"],
  plugins: ["prettier"],
  rules: {
    "no-console": "warn",
    "@typescript-eslint/no-angle-bracket-type-assertion": 0,
    "react-hooks/exhaustive-deps": 0,
    "prettier/prettier": [
      "error",
      {
        printWidth: 80,
        trailingComma: "all",
        singleQuote: false,
        bracketSpacing: true,
        tabWidth: 2,
        semi: true,
        endOfLine: "auto",
        arrowParens: "always",
      },
    ],
  },
};
