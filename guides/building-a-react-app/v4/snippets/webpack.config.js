//tag::garageEntry[]
var path = require('path');

module.exports = {
  entry: {
    index: './src/main/webapp/index.js', // <1>
    garage: './src/main/webapp/app/Garage.js' // <2>
  },
  //end::garageEntry[]
  //tag::outputSection[]
  output: {
    path: path.join(__dirname, 'grails-app/assets/javascripts'),
    publicPath: '/assets/',
    filename: 'bundle-[name].js'
  },
  //end::outputSection[]
  module: {
    rules: [
      {
        test: /\.js$/,
        include: path.join(__dirname, 'src/main/webapp'),
        use: {
          loader: 'babel-loader',
          options: {
            presets: ["react", ["es2015", {"modules": false}]],
            plugins: ["transform-class-properties"]
          }
        }
      },
      {
        test: /\.css$/,
        use: [
          'style-loader',
          'css-loader'
        ]
      },
      {
        test: /\.(jpe?g|png|gif|svg)$/i,
        use: {
          loader: 'url-loader?limit=10000&prefix=assets/!img'
        }
      }
    ]
  }
};

