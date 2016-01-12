// Generated on 2013-10-06 using generator-angular 0.4.0
'use strict';
var LIVERELOAD_PORT = 36729;
var mountFolder = function(connect, dir) {
  return connect.static(require('path').resolve(dir));
};
var proxySnippet = require('grunt-connect-proxy/lib/utils').proxyRequest;


module.exports = function(grunt) {
  grunt.loadNpmTasks('grunt-dev-prod-switch');
  grunt.loadNpmTasks('grunt-connect-proxy');
  grunt.loadNpmTasks('grunt-angular-templates');
  require('load-grunt-tasks')(grunt);
//require('time-grunt')(grunt);

  // configurable paths
  var yeomanConfig = {
    app: '../src/main/app',
    dist: '../target/yo/dist',
    test: '../src/test/js'
  };

  var tsconfig = require(yeomanConfig.app + '/tsconfig.json');
  var tsconfigTest = require(yeomanConfig.test + '/tsconfig.json');

  try {
    yeomanConfig.app = require('./bower.json').appPath || yeomanConfig.app;
  } catch (e) {

  }

  grunt.initConfig({
    dev_prod_switch: {
      options: {
        environment: 'dev',
        env_char: '#',
        env_block_dev: 'env:dev',
        env_block_prod: 'env:prod'
      },
      all: {
        files: {
          'eperusteet-ylops-app/src/main/app/scripts/views/opetussuunnitelmat/directives/header.js':
            'eperusteet-ylops-app/src/main/app/scripts/views/opetussuunnitelmat/directives/header.js'
        }
      }
    },
    yeoman: yeomanConfig,
    ts: {
      tests: {
        files: [{
          src: tsconfigTest.files.map(function(file) {
            return yeomanConfig.test + '/' + file;
          }),
          dest: yeomanConfig.test + '/',
          options: {
            module: 'amd',
            target: 'es5',
            sourceMap: true
          }
        }]
      },
      sources: {
        files: [{
          src: tsconfig.files.map(function(file) {
            return yeomanConfig.app + '/' + file;
          }),
          dest: yeomanConfig.app,
          options: {
            module: 'amd',
            target: 'es5',
            sourceMap: true
          }
        }]
      }
    },
    focus: {
      dev: {
        exclude: ['test']
      }
    },
    watch: {
      css: {
        files: ['<%= yeoman.app %>/styles/{,*/}*.scss'],
        tasks: ['sass', 'copy:fonts', 'autoprefixer']
      },
      test: {
        files: ['<%= yeoman.app %>/**/*.{ts,html}', '<%= yeoman.test %>/**/*.ts','!<%= yeoman.app %>/bower_components/**'],
        tasks: ['ts', 'karma:unit', 'regex-check']
      },
      livereload: {
        options: {
          livereload: LIVERELOAD_PORT,
          open: false
        },
        tasks: ['ts'],
        files: [
          '<%= yeoman.app %>/**/*.{html,ts}',
          '!<%= yeoman.app %>/bower_components/**',
          '.tmp/styles/**/*.css',
          '{.tmp,<%= yeoman.app %>}/scripts/**/*.ts',
          '<%= yeoman.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
        ]
      }
    },
    autoprefixer: {
      options: ['last 1 version'],
      dist: {
        files: [{
          expand: true,
          cwd: '.tmp/styles/',
          src: '{,*/}*.css',
          dest: '.tmp/styles/'
        }]
      }
    },
    connect: {
      options: {
        port: 9010,
        hostname: '0.0.0.0',
        livereload: LIVERELOAD_PORT
      },
      proxies: [{
        context: '/eperusteet-ylops-service',
        host: 'localhost',
        port: 8080,
        https: false,
        changeOrigin: false,
        xforward: false
      },{
        context: '/virkailija-raamit',
        host: 'localhost',
        port: 8080,
        https: false,
        changeOrigin: false,
        xforward: false
      }],
      livereload: {
        options: {
          base: ['.tmp', '<%= yeoman.app %>'],
          middleware: function(connect) {
            return [
              proxySnippet,
              mountFolder(connect, '.tmp'),
              mountFolder(connect, yeomanConfig.app)
            ];
          }
        }
      },
      test: {
        options: {
          port: 0,
          middleware: function(connect) {
            return [
              mountFolder(connect, '.tmp'),
              mountFolder(connect, yeomanConfig.test)
            ];
          }
        }
      },
      dist: {
        options: {
          middleware: function(connect) {
            return [
              mountFolder(connect, yeomanConfig.dist)
            ];
          }
        }
      }
    },
    // open: {
    //   server: {
    //     url: 'http://localhost:<%= connect.options.port %>'
    //   }
    // },
    clean: {
      options: {
        force: true, /* files outside working directory! */
      },
      dist: {
        files: [{
          dot: true,
          src: [
            '.tmp',
            '<%= yeoman.dist %>/*',
            '!<%= yeoman.dist %>/.git*'
          ]
        }]
      },
      server: '.tmp'
    },
    // not used since Uglify task does concat,
    // but still available if needed
    /*concat: {
     dist: {}
     },*/
    rev: {
      dist: {
        files: {
          src: [
            '<%= yeoman.dist %>/scripts/{,*/}*.js',
            '<%= yeoman.dist %>/styles/{,*/}*.css',
            '<%= yeoman.dist %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}',
            '<%= yeoman.dist %>/styles/fonts/*'
          ]
        }
      }
    },
    useminPrepare: {
      html: '<%= yeoman.app %>/index.html',
      options: {
        dest: '<%= yeoman.dist %>',
        flow: {
          html: {
            steps: {
              js: ['concat','uglifyjs'],
              css: ['cssmin']
            },
            post: {}
          }
        }
      }
    },
    usemin: {
      html: ['<%= yeoman.dist %>/*.html','<%= yeoman.dist %>/views/**/*.html'],
      css: ['<%= yeoman.dist %>/styles/{,*/}*.css'],
      js: [
        '<%= yeoman.dist %>/scripts/*.scripts.js',
        '<%= yeoman.dist %>/scripts/*.templates.js'
      ],
      options: {
        assetsDirs: ['<%= yeoman.dist %>','<%=yeoman.dist %>/styles'],
        patterns: {
          js: [
          [/\\?"(images\/.*?\.(png|gif|jpg|jpeg|svg))\\?"/g,'JS rev png images']
          ]
        }
      }
    },
    imagemin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.app %>/images',
          src: '{,*/}*.{png,jpg,jpeg}',
          dest: '<%= yeoman.dist %>/images'
        }]
      }
    },
    svgmin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.app %>/images',
          src: '{,*/}*.svg',
          dest: '<%= yeoman.dist %>/images'
        }]
      }
    },
    cssmin: {
      // By default, your `index.html` <!-- Usemin Block --> will take care of
      // minification. This option is pre-configured if you do not wish to use
      // Usemin blocks.
      // dist: {
      //   files: {
      //     '<%= yeoman.dist %>/styles/main.css': [
      //       '.tmp/styles/{,*/}*.css',
      //       '<%= yeoman.app %>/styles/{,*/}*.css'
      //     ]
      //   }
      // }
    },
    htmlmin: {
      dist: {
        options: {
          /*removeCommentsFromCDATA: true,
           // https://github.com/yeoman/grunt-usemin/issues/44
           //collapseWhitespace: true,
           collapseBooleanAttributes: true,
           removeAttributeQuotes: true,
           removeRedundantAttributes: true,
           useShortDoctype: true,
           removeEmptyAttributes: true,
           removeOptionalTags: true*/
        },
        files: [{
          expand: true,
          cwd: '<%= yeoman.app %>',
          src: ['*.html'],
          dest: '<%= yeoman.dist %>'
        }]
      }
    },
    // Put files not handled in other tasks here
    copy: {
      dist: {
        files: [{
          expand: true,
          dot: true,
          cwd: '<%= yeoman.app %>',
          dest: '<%= yeoman.dist %>',
          src: [
            '*.{ico,png,txt}',
            '.htaccess',
            'images/{,*/}*.{gif,webp}',
            'styles/fonts/*'
          ]
        }, {
          expand: true,
          cwd: '<%= yeoman.app %>/bower_components/ckeditor',
          dest: '<%= yeoman.dist %>/bower_components/ckeditor',
          src: [
            '**',
            '!samples/**'
          ]
        },{
            expand: true,
            cwd: '<%= yeoman.app %>/bower_components/ng-file-upload',
            dest: '<%= yeoman.dist %>/bower_components/ng-file-upload',
            src: [
                'FileAPI.flash.swf',
                'FileAPI.min.js'
            ]
        },{
          expand: true,
          cwd: '<%= yeoman.app %>/localisation',
          dest: '<%= yeoman.dist %>/localisation',
          src: [
            '*.json'
          ]
        }, {
          expand: true,
          cwd: '<%= yeoman.app %>/ckeditor-plugins',
          dest: '<%= yeoman.dist %>/ckeditor-plugins',
          src: [
            '**',
          ]
        }, {
          expand: true,
          cwd: '.tmp/images',
          dest: '<%= yeoman.dist %>/images',
          src: [
            'generated/*'
          ]
        }, {
          expand: true,
          cwd: '<%= yeoman.app %>/bower_components/bootstrap-sass-official/assets/fonts/bootstrap',
          dest: '<%= yeoman.dist %>/styles/fonts',
          src: '*.{eot,svg,ttf,woff}'
        }]
      },
      fonts: {
        expand: true,
        cwd: '<%= yeoman.app %>/bower_components/bootstrap-sass-official/assets/fonts/bootstrap',
        dest: '.tmp/styles/fonts/',
        src: '*.{eot,svg,ttf,woff}'
      }
    },
    concurrent: {
      server: [
        'sass'
      ],
      test: [
        'sass'
      ],
      dist: [
        'sass',
        'imagemin',
        'svgmin',
        'htmlmin'
      ]
    },
    karma: {
      unit: {
        configFile: 'karma.conf.js',
        singleRun: true
      }
    },
    cdnify: {
      dist: {
        html: ['<%= yeoman.dist %>/*.html']
      }
    },
    ngmin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.dist %>/scripts',
          src: '*.js',
          dest: '<%= yeoman.dist %>/scripts'
        }]
      }
    },
    uglify: {
      options: { mangle: false },
      dist: {
        files: {
          '.tmp/concat/scripts/scripts.js': [
            '<%= yeoman.dist %>/scripts/scripts.js'
          ]
        }
      }
    },
    sass: {
      dist: {
        files: {
          '.tmp/styles/eperusteet-ylops.css': '<%= yeoman.app %>/styles/eperusteet-ylops.scss'
        }
      }
    },
    ngtemplates: {
      dist: {
        cwd: '<%= yeoman.app %>',
        src: 'views/**/*.html',
        dest: '.tmp/views.js',
        append: true,
        options:    {
          module: 'ylopsApp',
          usemin: 'scripts/scripts.js',
          htmlmin: { collapseWhitespace: true, removeComments: true }
        }
      }
    },
    'regex-check': {
      templateurls: {
        files: [{src: ['<%= yeoman.app %>/scripts/**/*.js']}],
        options: {
          /* Check that templateUrls don't start with slash */
          pattern : /templateUrl:\s*['"]\//m
        },
      },
      showhide: {
        files: [{src: ['<%= yeoman.app %>/{scripts,views}/**/*.{js,html}']}],
        options: {
          /* Check that ng-show/ng-hide are not used in same element */
          pattern : /(ng-show=|ng-hide=)[^>]+(ng-hide=|ng-show=)/m
        },
      },
      // emptyHrefs: {
      //   files: [{src: ['<%= yeoman.app %>/{scripts,views}#<{(||)}>#*.{js,html}']}],
      //   options: {
      //     #<{(| Check that empty href="" are not used |)}>#
      //     pattern : /\s+href=""/m
      //   },
      // },
      controllerNaming: {
        files: [{src: ['<%= yeoman.app %>/scripts/**/*.js']}],
        options: {
          /* Enforce CamelCaseController naming */
          pattern : /\.controller\s*\(\s*'([a-z][^']+|([^'](?!Controller))+)'/g
        },
      }
    },
    maxlines: {
      options: {
        limit: 500
      },
      javascript: {
        options: {
          limit: 300
        },
        files: [{src: ['<%= yeoman.app %>/scripts/**/*.ts']}]
      },
      scss: {
        options: {
          limit: 500
        },
        files: [{src: ['<%= yeoman.app %>/styles/**/*.scss']}]
      }
    }
  });

  var devTask = function (excludeTests) {
    return function(target) {
      if (target === 'dist') {
        return grunt.task.run(['build', 'connect:dist:keepalive']);
      }

      grunt.task.run([
        'ts',
        'clean:server',
        'concurrent:server',
        'copy:fonts',
        'autoprefixer',
        'configureProxies',
        'connect:livereload',
        excludeTests ? 'focus:dev' : 'watch'
      ]);
    };
  };

  grunt.registerTask('server', devTask());
  grunt.registerTask('dev', devTask(true));

  grunt.registerTask('test', [
    'ts',
    'clean:server',
    'copy:fonts', // needed if testing while "grunt dev" is running :)
    'concurrent:test',
    'autoprefixer',
//  'connect:test',
    'regex-check',
    'maxlines',
    'karma'
  ]);

  grunt.registerTask('build', [
    'clean:dist',
    'ts',
    'useminPrepare',
    'concurrent:dist',
    'autoprefixer',
    'ngtemplates',
    'concat',
    'copy:dist',
//  'ngmin',
    'cssmin',
    'uglify',
    'rev',
    'usemin'
  ]);

  grunt.registerTask('default', [
    'ts',
    'test',
    'build'
  ]);
};
