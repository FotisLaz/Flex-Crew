/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        "my-grey": "#F5F5F7",
        "my-blue": "#0C356A",
        "yellow-alert": "#FFF0CE",
        "yellow-FlexCrew": "#EBB200",
        "black-stone": "#0C0B0B",
      },
      keyframes: {
        "modal-fade-in": {
          from: { opacity: "0", transform: "scale(0.95)" },
          to: { opacity: "1", transform: "scale(1)" },
        },
      },
      animation: {
        "modal-fade-in": "modal-fade-in 0.2s ease-out forwards",
      },
    },
  },
  plugins: [],
  safelist: [
    {
      pattern:
        /(bg|text|border)-(my-grey|my-blue|yellow-alert|black-stone|yellow-FlexCrew)/,
    },
  ],
};
